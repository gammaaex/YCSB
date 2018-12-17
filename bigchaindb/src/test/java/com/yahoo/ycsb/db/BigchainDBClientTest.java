package com.yahoo.ycsb.db;

import com.bigchaindb.api.AssetsApi;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.TreeMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BigchainDBClientTest {

  private final BigchainDBClient dbClient = new BigchainDBClient();
  private final String MOCK_TABLE = "'Table' is predetermined by BigchainDB";
  private final String MOCK_KEY = "This is mock key";
  private final String SEARCH_KEY = UUID.randomUUID().toString();
  private final TreeMap<String, ByteIterator> ASSETS = new TreeMap<>();

  @Before
  public void setUp() throws Exception {
    for (int i = 1; i <= 10; i++) {
      ASSETS.put(
          "field" + i,
          new StringByteIterator("value" + i)
      );
    }
    ASSETS.put("key", new StringByteIterator(SEARCH_KEY));

    dbClient.init();
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testRead() {
    try {
      dbClient.read(null, null, null, null);
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), is("Please use MongoDB module because BigchainDB is not support read operation."));
    }
  }

  @Test
  public void testScan() {
    try {
      dbClient.scan(null, null, 0, null, null);
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), is("Please use MongoDB module because BigchainDB is not support scan operation."));
    }
  }

  @Test
  public void testUpdate() {
    try {
      dbClient.update(null, null, null);
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), is("BigchainDB is not support update operation."));
    }
  }

  @Test
  public void testInsert() {
    Status status = dbClient.insert(
        MOCK_TABLE,
        MOCK_KEY,
        ASSETS
    );
    int assetSize = 0;

    try {
      Thread.sleep(300); // wait for insert
      assetSize = AssetsApi.getAssets(SEARCH_KEY).getAssets().size();
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }

    assertThat(Integer.parseInt("1"), is(assetSize));
    assertTrue(status.isOk());
  }

  @Test
  public void testDelete() {
    try {
      dbClient.delete(null, null);
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), is("BigchainDB is not support delete operation."));
    }
  }
}