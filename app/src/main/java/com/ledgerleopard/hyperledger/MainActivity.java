package com.ledgerleopard.hyperledger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.sun.jna.Native;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

	protected static final String POOL = "Pool1";
	protected static final String WALLET = "Wallet1";
	protected static final String TYPE = "default";

	private String TAG = MainActivity.class.getCanonicalName();

	static {
		//Native.register(MainActivity.class, Platform.C_LIBRARY_NAME);
		//NativeLibrary.addSearchPath(getApplicationInfo().nativeLibraryDir);
//		NativeLibrary.getInstance()
//		Native.register();
//
//
//		NativeLibrary.addSearchPath();
	}


//	public static void init(String searchPath) {
//		NativeLibrary.addSearchPath("indy", searchPath);
//		api = (LibIndy.API)Native.loadLibrary("indy", LibIndy.API.class);
//	}
//
//	public static void init(File file) {
//		api = (LibIndy.API)Native.loadLibrary(file.getAbsolutePath(), LibIndy.API.class);
//	}
//
//	public static void init() {
//		api = (LibIndy.API)Native.loadLibrary("indy", LibIndy.API.class);
//	}
//
//	public static boolean isInitialized() {
//		return api != null;
//	}




	public static native int getpid();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Native.DEBUG_LOAD = true;
		Native.DEBUG_JNA_LOAD = true;

		//System.loadLibrary("indy");
//		Log.e(TAG, "System.getProperty(\"java.class.path\"); = " + System.getProperty("java.class.path"));
//		System.setProperty("java.class.path", getApplicationInfo().nativeLibraryDir);
//		Log.e(TAG, "System.getProperty(\"java.class.path\"); = " + System.getProperty("java.class.path"));

		/*String libraryPath = getApplicationInfo().dataDir + "/lib";
		Log.e(TAG, "getApplicationInfo().dataDir/lib = " + libraryPath);
		if ( new File(getApplicationInfo().dataDir).exists() ){
			Log.e(TAG, libraryPath + " exists");
			printListOfFilesInDir(new File(getApplicationInfo().dataDir));
			Log.e(TAG, "*****************");
			printListOfFilesInDir(new File(getApplicationInfo().dataDir + File.separator + "lib"));
		}


		String libraryNativePath = getApplicationInfo().nativeLibraryDir;
		Log.e(TAG, "getApplicationInfo().nativeLibraryDir = " + libraryNativePath);
		if ( new File(libraryNativePath).exists() ){
			Log.e(TAG, libraryNativePath + " exists");
			printListOfFilesInDir(new File(libraryNativePath));
		}
*/



//		Native.register(MainActivity.class, Platform.C_LIBRARY_NAME);
//		NativeLibrary.addSearchPath("libindy.so", libraryNativePath);
//		NativeLibrary instance = NativeLibrary.getInstance(libraryNativePath + File.separator + "libindy.so");
//		Native.register(MainActivity.class, Platform.C_LIBRARY_NAME);

		//NativeLibrary.addSearchPath("", libraryNativePath);
		//Native.register(MainActivity.class, "indy");

		//System.setProperty("jna.library.path", libraryNativePath);
		//System.setProperty("jna.library.path", libraryPath);

//		File dataDir = getApplicationContext().getDataDir();
//		System.out.println("datadir=" + dataDir.getAbsolutePath());
//		File externalFilesDir = getExternalFilesDir(null);
//		System.out.println("externalFilesDir=" + externalFilesDir.getAbsolutePath());


		File externalFilesDir = getExternalFilesDir(null);
		System.out.println("externalFilesDir=" + externalFilesDir.getAbsolutePath());
		System.setProperty("user.home", externalFilesDir.getAbsolutePath());
		System.out.println("System.getProperty(user.home)= " + System.getProperty("user.home"));

		for (String s : System.getenv().keySet()) {
			Log.e(TAG, "Key = " + s  + " = " + System.getenv().get(s));
		}

		try {
			File file = new File("/system/app/tmp/");
			if ( !file.exists() ){
				boolean res = file.mkdirs();
				Log.e(TAG, "RES = " + res);
			}


			printListOfFilesInDir(new File("/system/app"));
		} catch (Exception e) {
			e.printStackTrace();
		}


		// try to set env variable HOME
		System.loadLibrary("indy");

		//Native.register(MainActivity.class, "indy");

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






//		try {
//			LibIndy.init();
//			Log.e(TAG, "LibIndy.init() - SUCCESS");
//		} catch (Exception e) {
//			Log.e(TAG, "LibIndy.init() - ERROR");
//		}

//		try {
//			File nativelibfile = new File(getApplicationInfo().nativeLibraryDir + File.separator + "libindy.so");
//			if ( nativelibfile.exists() ){
//				LibIndy.init(nativelibfile);
//				Log.e(TAG, "LibIndy.init() - SUCCESS");
//			}
//		} catch (Exception e) {
//			Log.e(TAG, "LibIndy.init() - ERROR");
//		}


//		if ( LibIndy.isInitialized() )
//			System.out.println( "Hello World!" );
//		else {
//			System.out.println( "Init error!" );
//			return;
//		}

//		try {
//			CompletableFuture<Void> wallet = Wallet.createWallet("PoolName", "name", "xtype", "config", "credentials");
//			wallet.get();
//			System.out.println( "Done" );
//
//		} catch (IndyException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}

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


	public Wallet wallet;

	public void createWallet() throws Exception {
		Wallet.createWallet(POOL, WALLET, TYPE, null, null).get();
		this.wallet = Wallet.openWallet(WALLET, null, null).get();
	}

	public void deleteWallet() throws Exception {
		if (wallet != null) {
			wallet.closeWallet().get();
			Wallet.deleteWallet(WALLET, null).get();
		}
	}
}