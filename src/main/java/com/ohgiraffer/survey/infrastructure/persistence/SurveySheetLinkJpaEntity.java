package com.ohgiraffer.survey.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "survey_sheet_link")
public class SurveySheetLinkJpaEntity
        extends BaseTimeEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "survey_sheet_link_id")
    private Long id;

    @Column(
            name = "survey_form_id",
            nullable = false,
            unique = true
    )
    private Long surveyFormId;

    @Column(
            name = "spreadsheet_url",
            nullable = false,
            length = 500
    )
    private String spreadsheetUrl;

    @Column(
            name = "spreadsheet_id",
            nullable = false,
            length = 255
    )
    private String spreadsheetId;

    @Column(
            name = "spreadsheet_title",
            nullable = false,
            length = 255
    )
    private String spreadsheetTitle;

    @Column(
            name = "sheet_gid",
            nullable = false
    )
    private Long sheetGid;

    @Column(
            name = "sheet_name",
            nullable = false,
            length = 255
    )
    private String sheetName;

    @Column(
            name = "respondent_column",
            nullable = false,
            length = 255
    )
    private String respondentColumn;

    @Column(
            name = "submitted_at_column",
            nullable = false,
            length = 255
    )
    private String submittedAtColumn;

    @Column(
            name = "linked_by",
            nullable = false
    )
    private Long linkedBy;

    protected SurveySheetLinkJpaEntity() {
    }

    private SurveySheetLinkJpaEntity(
            Long id,
            Long surveyFormId,
            String spreadsheetUrl,
            String spreadsheetId,
            String spreadsheetTitle,
            Long sheetGid,
            String sheetName,
            String respondentColumn,
            String submittedAtColumn,
            Long linkedBy
    ) {
        this.id = id;
        this.surveyFormId = surveyFormId;
        this.spreadsheetUrl = spreadsheetUrl;
        this.spreadsheetId = spreadsheetId;
        this.spreadsheetTitle = spreadsheetTitle;
        this.sheetGid = sheetGid;
        this.sheetName = sheetName;
        this.respondentColumn = respondentColumn;
        this.submittedAtColumn = submittedAtColumn;
        this.linkedBy = linkedBy;
    }

    public static SurveySheetLinkJpaEntity from(
            SurveySheetLink surveySheetLink
    ) {
        return new SurveySheetLinkJpaEntity(
                surveySheetLink.getId(),
                surveySheetLink.getSurveyFormId(),
                surveySheetLink.getSpreadsheetUrl(),
                surveySheetLink.getSpreadsheetId(),
                surveySheetLink.getSpreadsheetTitle(),
                surveySheetLink.getSheetGid(),
                surveySheetLink.getSheetName(),
                surveySheetLink.getRespondentColumn(),
                surveySheetLink.getSubmittedAtColumn(),
                surveySheetLink.getLinkedBy()
        );
    }

    public SurveySheetLink toDomain() {
        return SurveySheetLink.restore(
                id,
                surveyFormId,
                spreadsheetUrl,
                spreadsheetId,
                spreadsheetTitle,
                sheetGid,
                sheetName,
                respondentColumn,
                submittedAtColumn,
                linkedBy,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getSurveyFormId() {
        return surveyFormId;
    }
}