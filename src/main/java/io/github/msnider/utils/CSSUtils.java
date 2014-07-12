package io.github.msnider.utils;

import io.github.msnider.domain.UserAgent;

import java.util.List;

import com.phloc.css.decl.CSSExpression;
import com.phloc.css.decl.CSSExpressionMemberTermSimple;
import com.phloc.css.decl.CSSMediaExpression;
import com.phloc.css.decl.CSSMediaQuery;

public class CSSUtils {
	public static boolean matchesMediaQueries(List<CSSMediaQuery> mediaQueries, UserAgent userAgent) {
		if (mediaQueries == null || mediaQueries.isEmpty() || userAgent == null)
			return true;
		
		// Only need to match one media query to be true
		for(CSSMediaQuery mediaQuery : mediaQueries) {
			boolean isNot = mediaQuery.isNot();
			String medium = mediaQuery.getMedium();
			
			// Check that we match the Medium
			if ((!isNot && !userAgent.matchesMedium(medium)) || (isNot && userAgent.matchesMedium(medium)))
				continue;
			
			// Otherwise, we have to match every single feature
			if (matchesMediaExpressions(mediaQuery.getAllMediaExpressions(), userAgent))
				return true;
		}
		return false;
	}
	
	public static boolean matchesMediaExpressions(List<CSSMediaExpression> mediaExprs, UserAgent userAgent) {
		if (mediaExprs == null || mediaExprs.isEmpty() || userAgent == null)
			return true;
		
		// Must match all media expressions to be true
		for(CSSMediaExpression mediaExpr : mediaExprs) {
			String value = "";
			CSSExpression cssExpr = mediaExpr.getValue();
			if (cssExpr != null) {
				List<CSSExpressionMemberTermSimple> simpleTerms = cssExpr.getAllSimpleMembers();
				if (simpleTerms != null && !simpleTerms.isEmpty()) {
					CSSExpressionMemberTermSimple simpleTerm = simpleTerms.get(0);
					value = simpleTerm.getOptimizedValue();
				}
			}
			
			if (!userAgent.matchesFeature(mediaExpr.getFeature(), value)) {
				return false;
			}
		}
		return true;
	}
}
