package com.ohgiraffer.global.aop.lock;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/* comment.
 *  @DistributedLock(key = "...") 안의 SpEL 표현식을 실제 값으로 변환
 *  - 메서드 파라미터 이름 <-> 실행 시점 인자값을 매핑해서 컨텍스트에 등록 후 평가
 *  - ex. key = "'room:' + #roomId", args = [10L] -> "room:10"
 */

public class CustomSpringELParser {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private CustomSpringELParser() {
        // 유틸 클래스 - 인스턴스화 방지
    }

    public static Object getDynamicValue(String[] parameterNames, Object[] args, String expression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 파라미터 이름을 변수로 등록해야 SpEL에서 #paramName 형태로 참조 가능
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(expression).getValue(context, Object.class);
    }

}
