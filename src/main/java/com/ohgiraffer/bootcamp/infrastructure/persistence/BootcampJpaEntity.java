package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bootcamp_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BootcampJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "pro_name")
    private String proName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private BootcampJpaEntity(String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        this.orgName = orgName;
        this.proName = proName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 도메인 모델 → JPA 엔티티
    public static BootcampJpaEntity fromDomain(Bootcamp bootcamp) {
        return new BootcampJpaEntity(
                bootcamp.getOrgName(), bootcamp.getProName(), bootcamp.getStartDate(), bootcamp.getEndDate());
    }

    // JPA 엔티티 → 도메인 모델
    public Bootcamp toDomain() {
        return Bootcamp.reconstruct(id, orgName, proName, startDate, endDate);
    }

    public void update(String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        if (orgName != null) this.orgName = orgName;
        if (proName != null) this.proName = proName;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
    }
}

