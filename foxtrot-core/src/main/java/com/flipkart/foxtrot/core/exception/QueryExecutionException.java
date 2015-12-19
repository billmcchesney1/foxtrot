package com.flipkart.foxtrot.core.exception;

import com.flipkart.foxtrot.common.ActionRequest;

/**
 * Created by rishabh.goyal on 19/12/15.
 */
public class QueryExecutionException extends FoxtrotException {

    private ActionRequest actionRequest;

    public QueryExecutionException(ActionRequest actionRequest, Throwable cause) {
        super(ErrorCode.QUERY_EXECUTION_ERROR, cause);
        this.actionRequest = actionRequest;
    }

    public ActionRequest getActionRequest() {
        return actionRequest;
    }

    public void setActionRequest(ActionRequest actionRequest) {
        this.actionRequest = actionRequest;
    }
}
