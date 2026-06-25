package com.example.gezinio.auth.model;

public enum RoleName {
    /**
     * CUSTOMER: Normal müşteri
     * Sadece kendi işlemlerini yapabilir
     */
    CUSTOMER,

    /**
     * AGENT: Satış temsilcisi
     * Rezervasyon oluşturabilir, müşteri ekleyebilir
     */
    AGENT,

    /**
     * OFFICER: Operasyon görevlisi
     * Tur detaylarını güncelleyebilir, raporları görebilir
     */
    OFFICER,

    /**
     * MANAGER: Yönetici
     * Tur/otel ekleyebilir, finansal raporları görebilir
     */
    MANAGER,

    /**
     * SUPER_ADMIN: Sistem yöneticisi
     * Tüm yetkiler (kullanıcı yönetimi, sistem ayarları)
     */
    SUPER_ADMIN
}
