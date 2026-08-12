ALTER TABLE `attendance`
    ADD COLUMN `outing_time` TIME NULL COMMENT '외출시간' AFTER `check_out_time`,
    ADD COLUMN `return_time` TIME NULL COMMENT '복귀시간' AFTER `outing_time`;

ALTER TABLE `attendance_external_sheet_link`
    ADD COLUMN `date_cell_range` VARCHAR(200) NOT NULL COMMENT '선택된 날짜가 표시되는 셀 범위 (예: 출결현황!B2)' AFTER `tab_name`;