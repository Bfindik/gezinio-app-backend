package com.example.gezinio.auth.model;

public enum PermissionName {

    // ==================== USER MANAGEMENT ====================
    USER_CREATE,          // Yeni kullanıcı oluşturma
    USER_READ,            // Kullanıcı listesi görüntüleme
    USER_UPDATE,          // Kullanıcı bilgilerini güncelleme
    USER_DELETE,          // Kullanıcı silme (SUPER_ADMIN only)
    USER_MANAGE_ROLES,    // Rol atama/çıkarma (SUPER_ADMIN only)

    // ==================== CUSTOMER MANAGEMENT ====================
    CUSTOMER_CREATE,
    CUSTOMER_READ,
    CUSTOMER_UPDATE,
    CUSTOMER_DELETE,

    // ==================== TOUR MANAGEMENT ====================
    TOUR_CREATE,          // Yeni tur oluşturma
    TOUR_READ,            // Tur görüntüleme (herkes yapabilir)
    TOUR_UPDATE,          // Tur güncelleme
    TOUR_DELETE,          // Tur silme
    TOUR_PUBLISH,         // Tur yayınlama/yayından kaldırma

    // ==================== HOTEL MANAGEMENT ====================
    HOTEL_CREATE,
    HOTEL_READ,
    HOTEL_UPDATE,
    HOTEL_DELETE,

    // ==================== EXCURSION MANAGEMENT ====================
    EXCURSION_CREATE,
    EXCURSION_READ,
    EXCURSION_UPDATE,
    EXCURSION_DELETE,

    // ==================== TRANSFER MANAGEMENT ====================
    TRANSFER_CREATE,
    TRANSFER_READ,
    TRANSFER_UPDATE,
    TRANSFER_DELETE,

    // ==================== PASSENGER MANAGEMENT ====================
    PASSENGER_CREATE,     // Yolcu ekleme
    PASSENGER_READ,       // Yolcu listesi görme
    PASSENGER_UPDATE,     // Yolcu bilgilerini güncelleme
    PASSENGER_DELETE,     // Yolcu silme

    // ==================== RESERVATION MANAGEMENT ====================
    RESERVATION_CREATE,   // Rezervasyon oluşturma
    RESERVATION_READ,     // Rezervasyon görüntüleme
    RESERVATION_UPDATE,   // Rezervasyon güncelleme
    RESERVATION_DELETE,   // Rezervasyon silme
    RESERVATION_APPROVE,  // Rezervasyon onaylama
    RESERVATION_CANCEL,   // Rezervasyon iptal etme

    // ==================== PAYMENT OPERATIONS ====================
    PAYMENT_CREATE,      // Ödeme işlemi yapma
    PAYMENT_REFUND,       // Para iadesi (hassas!)
    PAYMENT_READ,         // Ödeme bilgilerini görme
    PAYMENT_MANAGE,       // Ödeme ayarları (SUPER_ADMIN)
    PAYMENT_UPDATE,

    // ==================== REPORT OPERATIONS ====================
    REPORT_READ,          // Raporları görüntüleme
    REPORT_EXPORT,        // Rapor export (Excel, PDF)
    REPORT_FINANCIAL,     // Finansal raporlar (hassas!)
    REPORT_CREATE,
    REPORT_UPDATE,
    REPORT_DELETE,

    // ==================== ADMIN OPERATIONS ====================
    ROLE_MANAGE,          // Rol oluşturma/düzenleme
    PERMISSION_MANAGE,    // Permission yönetimi
    SETTINGS_MANAGE,      // Sistem ayarları
    AUDIT_LOG_VIEW,       // Audit log görüntüleme

    // ==================== CUSTOMER OPERATIONS ====================
    MY_RESERVATIONS_VIEW, // Kendi rezervasyonlarını görme
    REVIEW_CREATE,        // Yorum yazma

    // ==================== CUSTOMER GROUP MANAGEMENT ====================
    CUSTOMER_GROUP_CREATE,
    CUSTOMER_GROUP_READ,
    CUSTOMER_GROUP_UPDATE,
    CUSTOMER_GROUP_DELETE,
    CUSTOMER_GROUP_MANAGE_MEMBERS
}
