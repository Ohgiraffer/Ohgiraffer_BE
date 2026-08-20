package com.ohgiraffer.evaluation.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 변경 목록을 화면이 그릴 카드로 바꾼다.
 *
 * <p>AI 를 쓰지 않는다. 무엇이 어떻게 바뀌었는지는 이미 값으로 들고 있어 지어낼 것이 없고,
 * 외부 호출이 실패했다고 화면이 비면 안 된다. AI 는 확인이 필요한 점을 짚는 데만 쓴다.
 *
 * <p>훈련생당 카드 하나다. 같은 사람이 여러 항목에서 바뀌면 한 카드 안에 모은다.
 */
public final class EvaluationDiffSummaryWriter {

    private static final String NO_CHANGE = "변경 없음";
    private static final String FRAGMENT_SEPARATOR = " / ";

    private EvaluationDiffSummaryWriter() {
    }

    /**
     * 훈련생별로 묶어 카드를 만든다. 시트에서 만난 순서를 유지한다.
     */
    public static List<TraineeChangeSummary> write(List<EvaluationChange> changes) {
        Map<String, List<EvaluationChange>> byTrainee = new LinkedHashMap<>();

        for (EvaluationChange change : changes) {
            byTrainee.computeIfAbsent(
                    change.traineeName(), name -> new ArrayList<>()
            ).add(change);
        }

        List<TraineeChangeSummary> summaries = new ArrayList<>();

        byTrainee.forEach((traineeName, traineeChanges) ->
                summaries.add(toSummary(traineeName, traineeChanges)));

        return summaries;
    }

    private static TraineeChangeSummary toSummary(
            String traineeName,
            List<EvaluationChange> changes
    ) {
        return new TraineeChangeSummary(
                traineeName,
                join(changes, EvaluationChange::evaluationType),
                join(changes, EvaluationChange::item),
                describeScores(changes),
                describeComments(changes),
                null
        );
    }

    /**
     * 같은 값이 여러 번 나오면 한 번만 적는다. 평가 유형은 대개 하나로 모인다.
     */
    private static String join(
            List<EvaluationChange> changes,
            java.util.function.Function<EvaluationChange, String> field
    ) {
        return String.join(", ", new LinkedHashSet<>(changes.stream()
                .map(field)
                .toList()));
    }

    private static String describeScores(List<EvaluationChange> changes) {
        List<String> fragments = new ArrayList<>();
        boolean single = changes.size() == 1;

        for (EvaluationChange change : changes) {
            String fragment = scoreFragment(change);

            if (fragment != null) {
                /*
                 * 항목이 하나면 어느 항목인지 이미 위 칸에 있어 다시 적지 않는다.
                 * 여럿일 때만 어느 항목의 변화인지 붙여야 구분된다.
                 */
                fragments.add(single ? fragment : change.item() + " " + fragment);
            }
        }

        return fragments.isEmpty() ? NO_CHANGE : String.join(FRAGMENT_SEPARATOR, fragments);
    }

    private static String scoreFragment(EvaluationChange change) {
        if (change.type() == EvaluationChange.Type.ADDED) {
            return format(change.newScore()) + " 신규";
        }

        if (!change.scoreChanged()) {
            return null;
        }

        return format(change.previousScore()) + " → " + format(change.newScore());
    }

    private static String describeComments(List<EvaluationChange> changes) {
        List<String> fragments = new ArrayList<>();
        boolean single = changes.size() == 1;

        for (EvaluationChange change : changes) {
            String fragment = commentFragment(change);

            if (fragment != null) {
                fragments.add(single ? fragment : change.item() + " " + fragment);
            }
        }

        return fragments.isEmpty() ? NO_CHANGE : String.join(FRAGMENT_SEPARATOR, fragments);
    }

    private static String commentFragment(EvaluationChange change) {
        if (change.type() == EvaluationChange.Type.ADDED) {
            return isBlank(change.newComment()) ? null : quote(change.newComment());
        }

        if (!change.commentChanged()) {
            return null;
        }

        return quote(change.previousComment()) + " → " + quote(change.newComment());
    }

    /**
     * 알림 본문처럼 카드를 그릴 수 없는 곳에서 쓸 한 덩어리 글.
     */
    public static String toText(List<TraineeChangeSummary> summaries) {
        if (summaries.isEmpty()) {
            return "변경된 평가가 없습니다.";
        }

        List<String> lines = new ArrayList<>();

        for (TraineeChangeSummary summary : summaries) {
            StringBuilder line = new StringBuilder()
                    .append(summary.traineeName())
                    .append(" · ")
                    .append(summary.item())
                    .append(" — 점수 ")
                    .append(summary.score());

            if (!NO_CHANGE.equals(summary.comment())) {
                line.append(", 의견 ").append(summary.comment());
            }

            if (!isBlank(summary.needsCheck())) {
                line.append(" (확인 필요: ").append(summary.needsCheck()).append(")");
            }

            lines.add(line.toString());
        }

        return String.join(System.lineSeparator(), lines);
    }

    /**
     * 점수는 보이는 대로 적는다. 85.00 을 그대로 두면 사람이 읽기 번거롭다.
     */
    private static String format(BigDecimal score) {
        if (score == null) {
            return "없음";
        }

        return score.stripTrailingZeros().toPlainString();
    }

    /**
     * 비어 있음을 나타내는 표시는 따옴표를 두르지 않는다. 시트에 "없음" 이라고 적힌 값과
     * 값이 비어 있는 경우가 글자로는 같아져 읽는 사람이 구분할 수 없기 때문이다.
     */
    private static String quote(String comment) {
        if (isBlank(comment)) {
            return "(비어 있음)";
        }

        return "\"" + comment + "\"";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
