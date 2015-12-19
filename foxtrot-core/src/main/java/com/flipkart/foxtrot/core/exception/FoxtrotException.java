package com.flipkart.foxtrot.core.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flipkart.foxtrot.common.ActionRequest;
import com.flipkart.foxtrot.common.Table;

import java.util.Collections;
import java.util.List;

/**
 * Created by rishabh.goyal on 13/12/15.
 */
public class FoxtrotException extends Exception {

    private ErrorCode code;

    public FoxtrotException(ErrorCode code) {
        this.code = code;
    }

    public FoxtrotException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public FoxtrotException(ErrorCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public FoxtrotException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public static TableInitializationException createTableInitializationException(Table table, String message) {
        return new TableInitializationException(table.getName(), message);
    }

    public static TableMissingException createTableMissingException(String table) {
        return new TableMissingException(table);
    }

    public static TableMissingException createTableMissingException(Table table, Exception e) {
        return new TableMissingException(table.getName(), e);
    }

    public static StoreConnectionException createConnectionException(Table table, Exception e) {
        return new StoreConnectionException(table.getName(), e);
    }

    public static BadRequestException createBadRequestException(String table, String message) {
        return createBadRequestException(table, Collections.singletonList(message));
    }

    public static BadRequestException createBadRequestException(String table, List<String> messages) {
        return new BadRequestException(table, messages);
    }

    public static BadRequestException createBadRequestException(Table table, Exception e) {
        return new BadRequestException(table.getName(),
                Collections.singletonList(String.format("%s - %s", table, e.getMessage())), e);
    }

    public static BadRequestException createBadRequestException(String table, Exception e) {
        return new BadRequestException(table,
                Collections.singletonList(String.format("%s - %s", table, e.getMessage())), e);
    }

    public static DocumentMissingException createMissingDocumentException(Table table, String id) {
        return new DocumentMissingException(table.getName(), Collections.singletonList(id));
    }

    public static DocumentMissingException createMissingDocumentsException(Table table, List<String> ids) {
        return new DocumentMissingException(table.getName(), ids);
    }

    public static StoreExecutionException createProcessingException(String table, Exception e) {
        return new StoreExecutionException(table, e);
    }

    public static QueryExecutionException createQueryExecutionException(ActionRequest actionRequest, Exception e) {
        return new QueryExecutionException(actionRequest, e);
    }

    public static TableExistsException createTableExistsException(String table) {
        return new TableExistsException(table);
    }

    public static DataCleanupException createDataCleanupException(String message, Exception e) {
        return new DataCleanupException(message, e);
    }

    public static QueryCreationException queryCreationException(ActionRequest actionRequest, Exception e) {
        return new QueryCreationException(actionRequest, e);
    }

    public static ActionResolutionException createActionResolutionException(ActionRequest actionRequest, Exception e) {
        return new ActionResolutionException(actionRequest, e);
    }

    public static UnresolvableActionException createUnresolvableActionException(ActionRequest actionRequest) {
        return new UnresolvableActionException(actionRequest);
    }

    @JsonIgnore
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }
}
