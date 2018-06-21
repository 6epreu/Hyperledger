package com.ledgerleopard.hyperledger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.gson.Gson;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

	protected static final String POOL = "docker-pool";
	protected static final String WALLET = "Wallet6";
	protected static final String TYPE = "default";
	protected static final String PAIRWISE_NAME = "Name";
	String credentials = "{\"key\": \"\"}";
	private String TAG = MainActivity.class.getCanonicalName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// try to set env variable HOME
		System.loadLibrary("crypto");
		System.loadLibrary("ssl");
		System.loadLibrary("sodium");
		System.loadLibrary("zmq");
		System.loadLibrary("indy");


		// set new path
		String internalPath = getFilesDir().getAbsolutePath();
		RustLib.INSTANCE.set_indy_home(internalPath);
		Log.e(TAG, "RustLib.INSTANCE.get_indy_home() = " + RustLib.INSTANCE.get_indy_home());

		// init
		LibIndy.init();
		if (!LibIndy.isInitialized()) {
			Log.e(TAG, "LibIndy NOT initialized ");
		} else {
			Log.e(TAG, "LibIndy INITIALIZED ");

			try {
				if ( !isWalletExist() ){
					createWallet();
				}

				createStoreMyDidAndConnectWithForeignDid("XCuwXpFwTP4aeALhn3DADJ");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (IndyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Wallet wallet;

	public void createWallet() throws Exception {
		CompletableFuture<Void> feature = Wallet.createWallet(POOL, WALLET, TYPE, null, credentials);
		feature.get();
		wallet = Wallet.openWallet(WALLET, null, credentials).get();
	}

	public boolean isWalletExist() {
		try {
			wallet = Wallet.openWallet(WALLET, null, credentials).get();
			return true;
		} catch (IndyException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return false;
	}

	public CompletableFuture<DidResults.CreateAndStoreMyDidResult> createDid() throws IndyException {
		// verKey = did
		String didOptions = new Gson().toJson(new CreateDidWalletRequest("", "", "", true));
		return Did.createAndStoreMyDid(wallet, didOptions);

	}

	public void deleteWallet() throws Exception {
		if (wallet != null) {
			wallet.closeWallet().get();
			Wallet.deleteWallet(WALLET, credentials).get();
		}
	}

	public interface RustLib extends Library {
		RustLib INSTANCE = (RustLib) Native.loadLibrary("indy", RustLib.class);
		void set_indy_home(String string);
		String get_indy_home();
	}

	public void printListOfFilesInDir( File folder ){
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

	}

	protected String GVT_SCHEMA_NAME = "gvt";
	protected String SCHEMA_VERSION = "1.0";
	protected String GVT_SCHEMA_ATTRIBUTES = "[\"name\", \"age\", \"sex\", \"height\"]";

	public void createStoreMyDidAndConnectWithForeignDid( String foreignDid ) {
		String jsonParams = new Gson().toJson(new CreateDidWalletRequest(null, null, null, true));
		try {
			// create my did
			DidResults.CreateAndStoreMyDidResult result = Did.createAndStoreMyDid(wallet, "{\"cid\":true}").get();

			if ( !checkIfTheirDidAlreadyStored(foreignDid) ){
				Did.storeTheirDid(wallet, String.format("{\"did\":\"%s\"}", foreignDid)).get();
			}

			if ( !Pairwise.isPairwiseExists(wallet, foreignDid).get() ){
				CompletableFuture<Void> feature = Pairwise.createPairwise(wallet, foreignDid, result.getDid(), PAIRWISE_NAME);
				feature.get();
				String listPairwise = Pairwise.listPairwise(wallet).get();
				Log.e(TAG, "SUCCESS");
			}

			String willEncryptYour = "test message";
			byte[] encryptedMessage = Crypto.anonCrypt(result.getVerkey(), willEncryptYour.getBytes()).get();
			byte[] decryptedMessage = Crypto.anonDecrypt(wallet, result.getVerkey(), encryptedMessage).get();
			String resultString = new String(decryptedMessage, Charset.forName("UTF-8"));
			Log.e(TAG, "Decrypted = "  + resultString);


			byte[] signature = Crypto.cryptoSign(wallet, result.getVerkey(), willEncryptYour.getBytes()).get();
			Boolean verified = Crypto.cryptoVerify(result.getVerkey(), willEncryptYour.getBytes(), signature).get();
			Log.e(TAG, "Verififcation = " + verified);

			String masterKey = Anoncreds.proverCreateMasterSecret(wallet, null).get();
			Log.e(TAG, "masterKey = " + masterKey);

//			// create and post credential schema
//			AnoncredsResults.IssuerCreateSchemaResult createSchemaResult = Anoncreds.issuerCreateSchema(result.getDid(), GVT_SCHEMA_NAME, SCHEMA_VERSION, GVT_SCHEMA_ATTRIBUTES).get();
//			String schema = createSchemaResult.getSchemaJson();
//			createSchemaResult.getSchemaId();
//			createSchemaResult.getSchemaId();
//
//			AnoncredsResults.IssuerCreateAndStoreCredentialDefResult mytag = Anoncreds.issuerCreateAndStoreCredentialDef(wallet, result.getDid(), createSchemaResult.getSchemaJson(), "mytag", null, "{\"support_revocation\":false}").get();
//			Log.e(TAG, "mytag.getCredDefJson() = " + mytag.getCredDefJson());
//
//			String offerResult = Anoncreds.issuerCreateCredentialOffer(wallet, mytag.getCredDefId()).get();
//			Log.e(TAG, "offerResult = " + offerResult);

		} catch (IndyException e) {
			Log.e(TAG, "ERROR = "  + e.getMessage());
		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR = "  + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "ERROR = "  + e.getMessage());
		}
	}

	/**
	 * I have not found functions in indy SDK that will allow to check if the foreign key already stored in the wallet
	 * But... every foreign/my key right now will be stored as a connection
	 * Also but... I know the output of the Pairwise.listPairwise(wallet).get() = "{"my_did":"EP51uAgmBThsnbch6Na5NZ","their_did":"XCuwXpFwTP4aeALhn3DADJ","metadata":"Name"}
	 * That means I can check only existans this key in this json output
	 * @param theirDid
	 * @return
	 */
	private boolean checkIfTheirDidAlreadyStored( String theirDid ) {
		try {
			String listPairwise = Pairwise.listPairwise(wallet).get();
			return listPairwise.contains(theirDid);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IndyException e) {
			e.printStackTrace();
		}

		return false;
	}
}