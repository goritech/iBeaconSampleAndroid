package tech.gori.ibeaconsample;

import android.os.RemoteException;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.util.Collection;
import java.util.Iterator;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

  // iBeaconのデータを認識するためのParserフォーマット
  private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
  private BeaconManager beaconManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    beaconManager = BeaconManager.getInstanceForApplication(this);
    // BeaconParseを設定
    beaconManager.getBeaconParsers()
        .add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
  }

  @Override protected void onPause() {
    super.onPause();
    beaconManager.unbind(this);
  }

  @Override protected void onResume() {
    super.onResume();
    beaconManager.bind(this);
  }

  @Override public void onBeaconServiceConnect() {
    // ビーコン監視
    try {
      // UUID
      String uuid = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
      Identifier identifierUuid = Identifier.parse(uuid);

      Region region = new Region("iBeaconSample", identifierUuid, null, null);

      // ビーコンエリアの監視
      beaconManager.startMonitoringBeaconsInRegion(region);
    } catch (RemoteException e) {
      e.printStackTrace();
      Log.d(this.getClass().getName(), e.getMessage());
    }

    // ビーコンエリア監視時の呼び出し
    beaconManager.addMonitorNotifier(new MonitorNotifier() {
      // エリアに入った時
      @Override public void didEnterRegion(Region region) {
        try {
          beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }

      // エリアから出た時
      @Override public void didExitRegion(Region region) {
        try {
          beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }

      // 現在のエリア内外の状態
      @Override public void didDetermineStateForRegion(int i, Region region) {
      }
    });

    // レンジング
    beaconManager.addRangeNotifier(new RangeNotifier() {
      @Override public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        // collectionにbeacon情報が入っている
        if (collection.size() > 0) {
          final Beacon beacon = (Beacon) collection.toArray()[0];

          runOnUiThread(new Runnable() {
            @Override public void run() {
              // UUID
              TextView uuidTextView = (TextView) findViewById(R.id.uuid);
              uuidTextView.setText(beacon.getId1().toString());

              // Major
              TextView majorTextView = (TextView) findViewById(R.id.major);
              majorTextView.setText(beacon.getId2().toString());

              // Minor
              TextView minorTextView = (TextView) findViewById(R.id.minor);
              minorTextView.setText(beacon.getId3().toString());

              // Rssi
              TextView rssiTextView = (TextView) findViewById(R.id.rssi);
              rssiTextView.setText(Integer.toString(beacon.getRssi()));

              // TxPower
              TextView txpowerTextView = (TextView) findViewById(R.id.txpower);
              txpowerTextView.setText(Integer.toString(beacon.getTxPower()));

              // Distance
              TextView distanceTextView = (TextView) findViewById(R.id.distance);
              distanceTextView.setText(Double.toString(beacon.getDistance()));
            }
          });
        }
      }
    });
  }


}
