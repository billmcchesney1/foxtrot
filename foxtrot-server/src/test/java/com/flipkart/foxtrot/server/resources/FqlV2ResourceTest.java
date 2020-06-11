package com.flipkart.foxtrot.server.resources;

import com.flipkart.foxtrot.common.TableActionRequestVisitor;
import com.flipkart.foxtrot.common.exception.ErrorCode;
import com.flipkart.foxtrot.common.exception.FoxtrotException;
import com.flipkart.foxtrot.common.exception.FqlParsingException;
import com.flipkart.foxtrot.core.config.QueryConfig;
import com.flipkart.foxtrot.gandalf.access.AccessServiceImpl;
import com.flipkart.foxtrot.server.providers.exception.FoxtrotExceptionMapper;
import com.flipkart.foxtrot.sql.FqlEngine;
import com.flipkart.foxtrot.sql.fqlstore.FqlStore;
import com.flipkart.foxtrot.sql.fqlstore.FqlStoreServiceImpl;
import com.flipkart.foxtrot.sql.responseprocessors.model.FlatRepresentation;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class FqlV2ResourceTest extends FoxtrotResourceTest {

    @Rule
    public ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new FqlV2Resource(
                    new FqlEngine(getTableMetadataManager(), getQueryStore(), getQueryExecutorFactory(), getMapper()),
                    new FqlStoreServiceImpl(getElasticsearchConnection(), getMapper()),
                    new AccessServiceImpl(false, new TableActionRequestVisitor()), new QueryConfig()))
            .addProvider(new FoxtrotExceptionMapper(getMapper()))
            .setMapper(objectMapper)
            .build();

    public FqlV2ResourceTest() throws IOException {
    }

    @Test
    public void testExecuteQuery() {
        String query = "show tables";
        Entity<String> stringEntity = Entity.json(query);
        FlatRepresentation flatRepresentation = resources.client()
                .target("/v2/fql")
                .request()
                .post(stringEntity, FlatRepresentation.class);

        Assert.assertNotNull(flatRepresentation);
        Assert.assertEquals(2, flatRepresentation.getHeaders()
                .size());
        Assert.assertEquals("name", flatRepresentation.getHeaders()
                .get(0)
                .getName());
        Assert.assertEquals("ttl", flatRepresentation.getHeaders()
                .get(1)
                .getName());
        Assert.assertEquals(1, flatRepresentation.getRows()
                .size());
        Assert.assertEquals("test-table", flatRepresentation.getRows()
                .get(0)
                .get("name"));
        Assert.assertEquals(7, flatRepresentation.getRows()
                .get(0)
                .get("ttl"));
        Assert.assertEquals(4, flatRepresentation.getRows()
                .get(0)
                .get("defaultRegions"));
    }

    @Test
    public void testExecuteQueryParseFail() {
        String query = "select * from test1 where";
        Entity<String> stringEntity = Entity.json(query);
        Response response = resources.client()
                .target("/v2/fql")
                .request()
                .post(stringEntity);
        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        FoxtrotException fqlParseError = response.readEntity(FqlParsingException.class);
        Assert.assertEquals(ErrorCode.FQL_PARSE_ERROR, fqlParseError.getCode());
    }

    @Test
    public void testNullResponseFromExecuteQuery() {
        String query = "select * from test1";
        Entity<String> stringEntity = Entity.json(query);
        Response response = resources.client()
                .target("/v2/fql")
                .request()
                .post(stringEntity);
        Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testSaveFqlStore() {
        String title = "testQuery";
        String query = "select * from test";

        FqlStore fqlStore = new FqlStore();
        fqlStore.setTitle(title);
        fqlStore.setQuery(query);
        Entity<FqlStore> fqlStoreEntity = Entity.json(fqlStore);
        FqlStore fqlStoreResponse = resources.client()
                .target("/v2/fql/save")
                .request()
                .post(fqlStoreEntity, FqlStore.class);

        Assert.assertNotNull(fqlStoreResponse);
        Assert.assertEquals(title, fqlStoreResponse.getTitle());
        Assert.assertEquals(query, fqlStoreResponse.getQuery());
    }

    /*@Ignore
    @Test
    public void testGetSavedFqlStore() throws InterruptedException {
        String title = "title1";
        String query = "show tables";

        FqlStore fqlStore = new FqlStore();
        fqlStore.setTitle(title);
        fqlStore.setQuery(query);
        Entity<FqlStore> fqlStoreEntity = Entity.json(fqlStore);

        FqlStore fqlStoreResponse = resources.client()
                .target("/v2/fql/save")
                .request()
                .post(fqlStoreEntity, FqlStore.class);
        Assert.assertNotNull(fqlStoreResponse);
        Assert.assertEquals(title, fqlStoreResponse.getTitle());
        Assert.assertEquals(query, fqlStoreResponse.getQuery());

        TimeUnit.SECONDS.sleep(1);

        FqlGetRequest fqlGetRequest = new FqlGetRequest();
        fqlGetRequest.setTitle(title);
        Entity<FqlGetRequest> fqlGetRequestEntity = Entity.json(fqlGetRequest);
        List<FqlStore> result = getMapper().convertValue(resources.client()
                .target("/v2/fql/get")
                .request()
                .post(fqlGetRequestEntity, List.class), new TypeReference<List<FqlStore>>() {
        });

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(title, result.get(0)
                .getTitle());
        Assert.assertEquals(query, result.get(0)
                .getQuery());
    }*/
}