package com.yahoo.ycsb.db;

import com.bigchaindb.api.TransactionsApi;
import com.bigchaindb.builders.BigchainDbConfigBuilder;
import com.bigchaindb.builders.BigchainDbTransactionBuilder;
import com.bigchaindb.constants.Operations;
import com.bigchaindb.model.FulFill;
import com.bigchaindb.model.GenericCallback;
import com.bigchaindb.model.MetaData;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import okhttp3.Response;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 */
public class BigchainDBClient extends DB {

  /**
   * @throws DBException
   */
  @Override
  public void init() throws DBException {
    // Configures connection url and credentials.
    BigchainDbConfigBuilder
        .baseUrl("http://localhost:9984") //or use http://testnet.bigchaindb.com
        .setup();
  }

  /**
   * read operation is not support this client. Please read that link.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to read.
   * @param fields The list of fields to read, or null for all of them
   * @param result A HashMap of field/value pairs for the result
   * @return
   * @see "http://docs.bigchaindb.com/en/latest/query.html"
   */
  @Override
  public Status read(String table, String key, Set<String> fields, Map<String, ByteIterator> result) {
    throw new UnsupportedOperationException(
        "Please use MongoDB module because BigchainDB is not support read operation."
    );
  }

  /**
   * scan operation is not support this client. Please read that link.
   *
   * @param table       The name of the table
   * @param startkey    The record key of the first record to read.
   * @param recordcount The number of records to read
   * @param fields      The list of fields to read, or null for all of them
   * @param result      A Vector of HashMaps, where each HashMap is a set field/value pairs for one record
   * @return
   * @see "http://docs.bigchaindb.com/en/latest/query.html"
   */
  @Override
  public Status scan(String table, String startkey, int recordcount,
                     Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
    throw new UnsupportedOperationException(
        "Please use MongoDB module because BigchainDB is not support scan operation."
    );
  }

  /**
   * performs TRANSFER operations on CREATED assets.
   * BigchainDB is immutable database. Therefore UPDATE operation is unsupported.
   * So, this is "Append" Operation.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to write.
   * @param values A HashMap of field/value pairs to update in the record
   * @return The result of the operation.
   */
  @Override
  public Status update(String table, String key, Map<String, ByteIterator> values) {
    KeyPair keyPair = createKeyPair();
    MetaData metaData = createMetaData(values);
    FulFill fulfill = new FulFill();
    fulfill.setTransactionId(key);
    fulfill.setOutputIndex(String.valueOf(0));

    try {
      BigchainDbTransactionBuilder
          .init()
          .addInput(null, fulfill, (EdDSAPublicKey) keyPair.getPublic())
          .addOutput("1", (EdDSAPublicKey) keyPair.getPublic())
          .addAssets(TransactionsApi.getTransactionById(key).getAsset().getId(), String.class)
          .addMetaData(metaData)
          .operation(Operations.TRANSFER)
          .buildAndSign((EdDSAPublicKey) keyPair.getPublic(), (EdDSAPrivateKey) keyPair.getPrivate())
          .sendTransaction(handleServerResponse());

      return Status.OK;
    } catch (IOException e) {
      e.printStackTrace();

      return Status.ERROR;
    }
  }

  /**
   * Insert a record in the database. This is called CREATE Transaction.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to insert.
   * @param values A HashMap of field/value pairs to insert in the record
   * @return The result of the operation.
   */
  @Override
  public Status insert(String table, String key, Map<String, ByteIterator> values) {
    Map<String, String> assets = createAssets(values);
    KeyPair keyPair = createKeyPair();

    try {
      BigchainDbTransactionBuilder
          .init()
          .addAssets(assets, TreeMap.class)
          .operation(Operations.CREATE)
          .buildAndSign((EdDSAPublicKey) keyPair.getPublic(), (EdDSAPrivateKey) keyPair.getPrivate())
          .sendTransaction(handleServerResponse());

      return Status.OK;
    } catch (IOException e) {
      e.printStackTrace();

      return Status.ERROR;
    }
  }

  /**
   * BigchainDB is immutable database. Therefore DELETE operation is unsupported.
   * So, this is "Burn" Operation.
   *
   * @param table The name of the table
   * @param key   The record key of the record to delete.
   * @return The result of the operation.
   */
  @Override
  public Status delete(String table, String key) {
    KeyPair keyPair = createKeyPair();
    MetaData metaData = new MetaData();
    metaData.setMetaData("status", "BURNED");
    FulFill fulfill = new FulFill();
    fulfill.setTransactionId(key);
    fulfill.setOutputIndex(String.valueOf(0));

    try {
      BigchainDbTransactionBuilder
          .init()
          .addInput(null, fulfill, (EdDSAPublicKey) keyPair.getPublic())
          .addOutput("1", (EdDSAPublicKey) keyPair.getPublic())
          .addAssets(TransactionsApi.getTransactionById(key).getAsset().getId(), String.class)
          .addMetaData(metaData)
          .operation(Operations.TRANSFER)
          .buildAndSign((EdDSAPublicKey) keyPair.getPublic(), (EdDSAPrivateKey) keyPair.getPrivate())
          .sendTransaction(handleServerResponse());

      return Status.OK;
    } catch (IOException e) {
      e.printStackTrace();

      return Status.ERROR;
    }
  }

  /**
   * Generates EdDSA keypair to sign and verify transactions.
   *
   * @return KeyPair
   */
  private KeyPair createKeyPair() {
    //  prepare your keys
    net.i2p.crypto.eddsa.KeyPairGenerator edDsaKpg = new net.i2p.crypto.eddsa.KeyPairGenerator();
    KeyPair keyPair = edDsaKpg.generateKeyPair();

    return keyPair;
  }

  /**
   * Create Asset Data from Map.
   * Asset data is the immutable data.
   *
   * @param values immutable data
   * @return asset data
   */
  private Map<String, String> createAssets(Map<String, ByteIterator> values) {
    Map<String, String> assets = new TreeMap<>();

    for (Map.Entry<String, ByteIterator> value : values.entrySet()) {
      assets.put(value.getKey(), value.getValue().toString());
    }

    return assets;
  }

  /**
   * Create Meta data from Map.
   * Meta data is the mutable data.
   *
   * @param values mutable data
   * @return meta data
   */
  private MetaData createMetaData(Map<String, ByteIterator> values) {
    MetaData metaData = new MetaData();

    for (Map.Entry<String, ByteIterator> value : values.entrySet()) {
      metaData.setMetaData(value.getKey(), value.getValue().toString());
    }

    return metaData;
  }

  /**
   * Define callback methods to verify response from BigchainDBServer.
   *
   * @return Callback
   */
  private GenericCallback handleServerResponse() {
    return new GenericCallback() {
      @Override
      public void transactionMalformed(Response response) {
        onFailure(response);
      }

      @Override
      public void pushedSuccessfully(Response response) {
        onSuccess(response);
      }

      @Override
      public void otherError(Response response) {
        onFailure(response);
      }
    };
  }

  /**
   * Do not use this.
   *
   * @param response HTTP Response
   */
  private void onSuccess(Response response) {
  }

  /**
   * Stop this program for benchmark.
   */
  private void onFailure(Response response) {
    try {
      throw new DBException("Transaction failed. Response is : " + response.body().string());
    } catch (IOException | DBException e) {
      e.printStackTrace();
    }
    System.exit(1);
  }
}