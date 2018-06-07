package com.ledgerleopard.hyperledger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

	protected static final String POOL = "docker-pool";
	protected static final String WALLET = "Wallet5";
	protected static final String TYPE = "default";
	String credentials = "{\"key\":\"testkey\"}";
	private String TAG = MainActivity.class.getCanonicalName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			Os.setenv("RUST_LOG", "trace", true);
			String environment = Os.getenv("RUST_LOG");
			Log.e(TAG, "RUST_LOG " + environment );
		} catch (ErrnoException e) {
			e.printStackTrace();
		}


//		// empty json config
//		Did.createAndStoreMyDid()
//
//
//		// provide NAME of connection as a metadata
//		Pairwise.createPairwise()


		// try to set env variable HOME
		System.loadLibrary("crypto");
		System.loadLibrary("ssl");
		System.loadLibrary("sodium");
		System.loadLibrary("zmq");
		System.loadLibrary("indy");


		String internalPath = getFilesDir().getAbsolutePath();
		Log.e(TAG, "getFilesDir().getAbsolutePath() = " + internalPath);

		RustLib.INSTANCE.set_indy_home(internalPath);
		Log.e(TAG, "RustLib.INSTANCE.get_indy_home() = " + RustLib.INSTANCE.get_indy_home());

		printListOfFilesInDir( new File(internalPath));

		LibIndy.init();
		if (!LibIndy.isInitialized()){
			Log.e(TAG, "LibIndy NOT initialized ");
		} else {
			Log.e(TAG, "LibIndy INITIALIZED ");
			try {
				createWallet();
				deleteWallet();
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
		CompletableFuture<Void> wallet = Wallet.createWallet(POOL, WALLET, TYPE, null, null);
		wallet.get();
		this.wallet = Wallet.openWallet(WALLET, null, null).get();
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
}