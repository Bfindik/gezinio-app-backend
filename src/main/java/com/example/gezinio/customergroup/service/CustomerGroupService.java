package com.example.gezinio.customergroup.service;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.model.UserType;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.auth.security.UserPrincipal;
import com.example.gezinio.customergroup.dto.AddMemberRequest;
import com.example.gezinio.customergroup.dto.CreateCustomerGroupRequest;
import com.example.gezinio.customergroup.dto.CustomerGroupDTO;
import com.example.gezinio.customergroup.dto.CustomerGroupMemberDTO;
import com.example.gezinio.customergroup.dto.UpdateCustomerGroupRequest;
import com.example.gezinio.customergroup.model.CustomerGroup;
import com.example.gezinio.customergroup.model.CustomerGroupMember;
import com.example.gezinio.customergroup.model.CustomerGroupType;
import com.example.gezinio.customergroup.model.MemberRelationship;
import com.example.gezinio.customergroup.repository.CustomerGroupMemberRepository;
import com.example.gezinio.customergroup.repository.CustomerGroupRepository;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerGroupService {

    private static final String BASE_CURRENCY = "TRY";

    private final CustomerGroupRepository groupRepository;
    private final CustomerGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public CustomerGroupService(CustomerGroupRepository groupRepository,
                                CustomerGroupMemberRepository memberRepository,
                                UserRepository userRepository,
                                ReservationRepository reservationRepository,
                                PaymentRepository paymentRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    // ============================================================
    // READ
    // ============================================================

    @Transactional(readOnly = true)
    public List<CustomerGroupDTO> getAll() {
        return groupRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerGroupDTO getById(Long id) {
        return toDTO(findGroup(id));
    }

    // ============================================================
    // CREATE / UPDATE / DELETE
    // ============================================================

    @Transactional
    public CustomerGroupDTO create(CreateCustomerGroupRequest request) {
        if (request.getMembers() == null || request.getMembers().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A customer group must have at least 2 members");
        }

        User primary = findCustomer(request.getPrimaryContactId());

        // Primary contact must appear in members[] as HEAD
        long headCount = request.getMembers().stream()
                .filter(m -> m.getRelationship() == MemberRelationship.HEAD)
                .count();
        if (headCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Exactly one member must have relationship HEAD");
        }
        AddMemberRequest headEntry = request.getMembers().stream()
                .filter(m -> m.getRelationship() == MemberRelationship.HEAD)
                .findFirst().get();
        if (!headEntry.getUserId().equals(primary.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "HEAD member must match primaryContactId");
        }

        // Unique users
        Set<Long> seen = new HashSet<>();
        for (AddMemberRequest m : request.getMembers()) {
            if (!seen.add(m.getUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate member userId: " + m.getUserId());
            }
        }

        CustomerGroup group = new CustomerGroup();
        group.setName(request.getName());
        group.setType(request.getType());
        group.setNotes(request.getNotes());
        group.setPrimaryContact(primary);
        group.setGroupCode(generateGroupCode(request.getType()));
        group.setCreatedBy(currentUser());

        // Persist first so members can reference it
        CustomerGroup saved = groupRepository.save(group);

        for (AddMemberRequest m : request.getMembers()) {
            User u = findCustomer(m.getUserId());
            CustomerGroupMember member = new CustomerGroupMember();
            member.setGroup(saved);
            member.setUser(u);
            member.setRelationship(m.getRelationship());
            saved.getMembers().add(member);
        }
        memberRepository.saveAll(saved.getMembers());

        return toDTO(saved);
    }

    @Transactional
    public CustomerGroupDTO update(Long id, UpdateCustomerGroupRequest request) {
        CustomerGroup group = findGroup(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            group.setName(request.getName());
        }
        if (request.getType() != null) {
            group.setType(request.getType());
        }
        if (request.getNotes() != null) {
            group.setNotes(request.getNotes());
        }
        if (request.getPrimaryContactId() != null
                && !request.getPrimaryContactId().equals(group.getPrimaryContact().getId())) {
            // The new primary contact must already be a member; promote them to HEAD,
            // demote the previous HEAD to MEMBER.
            CustomerGroupMember newHead = group.getMembers().stream()
                    .filter(m -> m.getUser().getId().equals(request.getPrimaryContactId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "New primary contact must already be a group member"));

            group.getMembers().stream()
                    .filter(m -> m.getRelationship() == MemberRelationship.HEAD)
                    .forEach(m -> m.setRelationship(MemberRelationship.MEMBER));
            newHead.setRelationship(MemberRelationship.HEAD);
            group.setPrimaryContact(newHead.getUser());
        }

        return toDTO(groupRepository.save(group));
    }

    @Transactional
    public void delete(Long id) {
        CustomerGroup group = findGroup(id);
        // orphanRemoval on members handles cleanup of join rows; user accounts stay intact.
        groupRepository.delete(group);
    }

    // ============================================================
    // MEMBERSHIP
    // ============================================================

    @Transactional
    public CustomerGroupDTO addMember(Long groupId, AddMemberRequest request) {
        CustomerGroup group = findGroup(groupId);
        User user = findCustomer(request.getUserId());

        if (memberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User is already a member of this group");
        }

        MemberRelationship rel = request.getRelationship();
        if (rel == MemberRelationship.HEAD) {
            // Adding someone as HEAD = make them the primary contact, demote old HEAD
            group.getMembers().stream()
                    .filter(m -> m.getRelationship() == MemberRelationship.HEAD)
                    .forEach(m -> m.setRelationship(MemberRelationship.MEMBER));
            group.setPrimaryContact(user);
        }

        CustomerGroupMember member = new CustomerGroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRelationship(rel);
        memberRepository.save(member);
        group.getMembers().add(member);

        return toDTO(groupRepository.save(group));
    }

    @Transactional
    public CustomerGroupDTO setRelationship(Long groupId, Long userId, MemberRelationship relationship) {
        CustomerGroup group = findGroup(groupId);
        CustomerGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User is not a member of this group"));

        if (relationship == MemberRelationship.HEAD) {
            group.getMembers().stream()
                    .filter(m -> m.getRelationship() == MemberRelationship.HEAD
                            && !m.getUser().getId().equals(userId))
                    .forEach(m -> m.setRelationship(MemberRelationship.MEMBER));
            member.setRelationship(MemberRelationship.HEAD);
            group.setPrimaryContact(member.getUser());
        } else {
            // Demoting HEAD is only legal if another member becomes HEAD in the same call.
            // To stay simple, require an explicit promotion of someone else first.
            if (member.getRelationship() == MemberRelationship.HEAD) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot demote HEAD directly — promote another member to HEAD instead");
            }
            member.setRelationship(relationship);
        }

        return toDTO(groupRepository.save(group));
    }

    @Transactional
    public CustomerGroupDTO removeMember(Long groupId, Long userId) {
        CustomerGroup group = findGroup(groupId);
        CustomerGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User is not a member of this group"));

        if (group.getMembers().size() <= 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot remove member — a group requires at least 2 members. Disband the group instead.");
        }

        if (member.getRelationship() == MemberRelationship.HEAD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot remove HEAD — promote another member to HEAD first");
        }

        group.getMembers().remove(member);
        memberRepository.delete(member);

        return toDTO(groupRepository.save(group));
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private CustomerGroup findGroup(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Customer group not found: " + id));
    }

    private User findCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + userId));
        if (user.getUserType() != UserType.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only CUSTOMER accounts can join a customer group (user " + userId + " is " + user.getUserType() + ")");
        }
        return user;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return userRepository.findById(principal.getId()).orElse(null);
    }

    private String generateGroupCode(CustomerGroupType type) {
        String prefix = (type == CustomerGroupType.FAMILY) ? "FAM" : "GRP";
        long count = groupRepository.countByGroupCodeStartingWith(prefix + "-");
        // Walk forward until we find an unused number — collisions are rare but possible
        // under concurrent creates because the count read isn't atomic.
        long seq = count + 1;
        String code;
        do {
            code = String.format("%s-%04d", prefix, seq++);
        } while (groupRepository.existsByGroupCode(code));
        return code;
    }

    // ============================================================
    // DTO mapping + aggregates
    // ============================================================

    private CustomerGroupDTO toDTO(CustomerGroup group) {
        CustomerGroupDTO dto = new CustomerGroupDTO();
        dto.setId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setName(group.getName());
        dto.setType(group.getType().name());
        dto.setNotes(group.getNotes());

        User primary = group.getPrimaryContact();
        if (primary != null) {
            dto.setPrimaryContactId(primary.getId());
            dto.setPrimaryContactName(fullName(primary));
            dto.setPrimaryContactEmail(primary.getEmail());
        }

        Long primaryId = primary != null ? primary.getId() : null;
        List<CustomerGroupMemberDTO> memberDtos = new ArrayList<>();
        long totalRes = 0;
        long confirmedRes = 0;
        long cancelledRes = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (CustomerGroupMember member : group.getMembers()) {
            CustomerGroupMemberDTO mDto = toMemberDTO(member, primaryId);
            memberDtos.add(mDto);

            totalRes += mDto.getTotalReservations();
            confirmedRes += mDto.getConfirmedReservations();
            cancelledRes += mDto.getCancelledReservations();
            totalSpent = totalSpent.add(mDto.getTotalSpent());
        }
        memberDtos.sort(Comparator.comparing(CustomerGroupMemberDTO::getAddedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));

        dto.setMembers(memberDtos);
        dto.setMemberCount(memberDtos.size());
        dto.setTotalBookings(totalRes);
        dto.setConfirmedBookings(confirmedRes);
        dto.setCancelledBookings(cancelledRes);
        dto.setCombinedSpent(totalSpent);
        dto.setCurrency(BASE_CURRENCY);

        if (group.getCreatedBy() != null) {
            dto.setCreatedBy(group.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        return dto;
    }

    private CustomerGroupMemberDTO toMemberDTO(CustomerGroupMember member, Long primaryUserId) {
        User user = member.getUser();
        CustomerGroupMemberDTO dto = new CustomerGroupMemberDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(fullName(user));
        dto.setPhone(user.getPhone());
        dto.setRelationship(member.getRelationship().name());
        dto.setPrimary(primaryUserId != null && primaryUserId.equals(user.getId()));
        dto.setAddedAt(member.getAddedAt());

        Long userId = user.getId();
        long total = reservationRepository.countByUserId(userId);
        long confirmed = reservationRepository.countByUserIdAndReservationStatus(userId, ReservationStatus.CONFIRMED);
        long cancelled = reservationRepository.countByUserIdAndReservationStatus(userId, ReservationStatus.CANCELLED);
        double spent = reservationRepository.findByUserId(userId).stream()
                .mapToDouble(r -> paymentRepository.sumCompletedPaymentsByReservationId(r.getId()).doubleValue())
                .sum();

        dto.setTotalReservations(total);
        dto.setConfirmedReservations(confirmed);
        dto.setCancelledReservations(cancelled);
        dto.setTotalSpent(BigDecimal.valueOf(spent));
        return dto;
    }

    private String fullName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        return (first + " " + last).trim();
    }
}
