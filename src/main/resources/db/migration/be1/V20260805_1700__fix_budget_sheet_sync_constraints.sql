DELETE t1
FROM `external_sheet_link` t1
JOIN `external_sheet_link` t2
  ON t1.`domain` = t2.`domain`
 AND (
      COALESCE(t1.`last_synced_at`, '1000-01-01 00:00:00') < COALESCE(t2.`last_synced_at`, '1000-01-01 00:00:00')
      OR (
          COALESCE(t1.`last_synced_at`, '1000-01-01 00:00:00') = COALESCE(t2.`last_synced_at`, '1000-01-01 00:00:00')
          AND t1.`sheet_link_id` < t2.`sheet_link_id`
      )
 );