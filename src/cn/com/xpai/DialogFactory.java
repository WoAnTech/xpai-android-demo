package cn.com.xpai;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xpai.core.Manager;

public class DialogFactory {

	private static final String TAG = "DialogFactory";
	private static Context mContext = null;

	public static final int CONNECTION_DIALOG = 0x20001;
	public static final int ALERT_DIALOG = 0x20002;
	public static final int ERR_DIALOG = 0x20003;
	public static final int CONNECTION_FAILED_DIALOG = 0x20004;
	public static final int LOGIN_DIALOG = 0x20005;
	public static final int MSG_DIALOG = 0x20006;
	public static final int INFO_DIALOG = 0x20007;
	public static final int SETTING_DIALOG = 0x20008;
	private static final String[] connectionModes = {"连接直播云", "连接私有云", "连接视频服务器"};
	private static View connectionEntryView;
	private static CheckBox connectTcpCheckBox;
	private static TextView mvHostTV;
	private static TextView mvPortTV;
	private static EditText mvCodeET;
	private static EditText mvHostET;
	private static EditText mvPortET;

	public static void register(Context context) {
		mContext = context;

	}

	public static Dialog getInstance(int dialogID) {
		return getInstance(dialogID, null);
	}

	public static Dialog getInstance(int dialogID, String msgStr) {
		if (mContext == null) {
			Message msg = new Message();
			msg.what = XPHandler.ERR_NOT_REGISTER_DIALOG_FACTORY;
			XPHandler.getInstance().sendMessage(msg);
			return null;
		}
		switch (dialogID) {
		case CONNECTION_DIALOG:
			return ConnectionDialog();
		case ALERT_DIALOG:
			return AlertDialog(msgStr);
		case ERR_DIALOG:
			return ErrorDialog(msgStr);
		case CONNECTION_FAILED_DIALOG:
			return ConnectionFailedDialog(msgStr);
		case LOGIN_DIALOG:
			return LoginDialog();
		case INFO_DIALOG:
			return infoDialog();
		default:
			return null;
		}
	}

	private static Dialog infoDialog() {
		Dialog dialog = new Dialog(mContext);
		dialog.findViewById(R.layout.info);
		return dialog;
	}

	private static Dialog ConnectionDialog() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		connectionEntryView = factory.inflate(
				R.layout.connection_dialog, null);
		connectTcpCheckBox = (CheckBox) connectionEntryView
				.findViewById(R.id.mvconnect_tcp_checkbox);
		mvHostTV = (TextView) connectionEntryView
				.findViewById(R.id.mvhost_view);
		mvPortTV = (TextView) connectionEntryView
				.findViewById(R.id.mvport_view);
		mvCodeET = (EditText) connectionEntryView
				.findViewById(R.id.mvcode_edit);
		mvHostET = (EditText) connectionEntryView
				.findViewById(R.id.mvhost_edit);
		mvPortET = (EditText) connectionEntryView
				.findViewById(R.id.mvport_edit);
		Spinner mSpinner = (Spinner) connectionEntryView
				.findViewById(R.id.connection_mode_spinner);
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, connectionModes);
		mSpinner.setAdapter(mAdapter);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
		for (int position=0;position<connectionModes.length;position++) {
			if (Config.connectionMode == position) {
				mSpinner.setSelection(position);
				break;
			}
		}

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Config.connectionMode = position;
				updateView();
				Config.save();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});

		updateView();

		if(Config.isConnectToTcpPort) {
			connectTcpCheckBox.setChecked(true);
		} else {
			connectTcpCheckBox.setChecked(false);
		}
		
		Dialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string.connection_dialog_title)
				.setView(connectionEntryView).setPositiveButton(
						R.string.connection_dialog_connect,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String mCode = mvCodeET.getText().toString();
								String mHost = mvHostET.getText().toString();
								String mPort = mvPortET.getText().toString();
								Config.serviceCode = mCode;
								Config.mvPort = mPort;
								if(Config.connectionMode == 2 &&
										(mPort == null || "".equals(mPort))) {
									Toast.makeText(mContext, "端口号不能为空!", Toast.LENGTH_LONG).show();
									return;
								}

								if (Config.connectionMode == 2) {
									Config.mvHost = mHost;
									if (Config.isConnectToTcpPort) {
										try {
											Manager.connectVS(mHost, Integer.parseInt(mPort), 
														Config.netTimeout * 1000, Config.serviceCode, 0);
										} catch (NumberFormatException e) {
											// TODO Auto-generated catch block
											Toast.makeText(mContext, "非法的端口号!", Toast.LENGTH_LONG).show();
											e.printStackTrace();
										}
									} else {
										try {
											Manager.initNet(mHost, Integer.parseInt(mPort), 
														Config.netTimeout * 1000, Config.serviceCode, 0);
										} catch (NumberFormatException e) {
											// TODO Auto-generated catch block
											Toast.makeText(mContext, "非法的端口号!", Toast.LENGTH_LONG).show();
											e.printStackTrace();
										}
									}
								} else if(Config.connectionMode == 0) {
									Manager.connectCloud(Config.getVSUrl,
											Config.netTimeout * 1000, Config.serviceCode, 0);
								} else {
									Config.privateCloudGetVSUrl = mHost;
									Manager.connectCloud(Config.privateCloudGetVSUrl,
											Config.netTimeout * 1000, Config.serviceCode, 0);
								}
								Config.save();
							}
						}).setNegativeButton(R.string.connection_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if(connectTcpCheckBox.isChecked()){
									Config.isConnectToTcpPort = true;
								} else {
									Config.isConnectToTcpPort = false;
								}
								Config.save();
							}
						}).create();
		mvCodeET.setText(Config.serviceCode);
		mvPortET.setText(Config.mvPort);
		
		connectTcpCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				Config.isConnectToTcpPort = isChecked;
				Config.save();
			}
		});
		return dialog;
	}

	private static void updateView() {
		switch(Config.connectionMode) {
		case 0:
			mvPortTV.setVisibility(View.GONE);
			mvPortET.setVisibility(View.GONE);
			connectTcpCheckBox.setVisibility(View.GONE);
			mvHostET.setVisibility(View.GONE);
			mvHostTV.setVisibility(View.GONE);
			break;
		case 1:
			mvPortTV.setVisibility(View.GONE);
			mvPortET.setVisibility(View.GONE);
			connectTcpCheckBox.setVisibility(View.GONE);
			mvHostET.setVisibility(View.VISIBLE);
			mvHostTV.setVisibility(View.VISIBLE);
			mvHostET.setText(Config.privateCloudGetVSUrl);
			mvHostTV.setText("getVS地址");
			break;
		case 2:
			mvPortTV.setVisibility(View.VISIBLE);
			mvPortET.setVisibility(View.VISIBLE);
			connectTcpCheckBox.setVisibility(View.VISIBLE);
			mvHostET.setVisibility(View.VISIBLE);
			mvHostTV.setVisibility(View.VISIBLE);
			mvHostET.setText(Config.mvHost);
			mvHostTV.setText("主机地址");
			break;
		default:
			break;
		}
	}

	private static Dialog AlertDialog(String msg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.alert)
				.setTitle(R.string.msg_dialog_title).setMessage(msg)
				.setPositiveButton(R.string.msg_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked OK so do some stuff */
							}
						}).create();
	}

	private static Dialog ErrorDialog(String errMsg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.stop)
				.setTitle(R.string.error_dialog_title).setMessage(errMsg)
				.setPositiveButton(R.string.error_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked OK so do some stuff */
							}
						}).create();
	}



	private static Dialog ConnectionFailedDialog(String errMsg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.stop)
				.setTitle(R.string.error_dialog_title).setMessage(errMsg)
				.setPositiveButton(R.string.connection_failed_dialog_connect,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								ConnectionDialog().show();
							}
						}).setNegativeButton(
						R.string.connection_failed_dialog_exit,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Message msg = new Message();
								msg.what = XPHandler.EXIT_APP;
								XPHandler.getInstance().sendMessage(msg);
							}
						}).create();
	}

	private static Dialog LoginDialog() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		final View loginView = factory.inflate(R.layout.login_dialog, null);
		Dialog loginDialog = new AlertDialog.Builder(mContext).setTitle(R.string.login_dialog_title).setView(
				loginView).setPositiveButton(R.string.login_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String mUserName = ((EditText) loginView
								.findViewById(R.id.mv_username_edit)).getText()
								.toString();
						String mPassword = ((EditText) loginView
								.findViewById(R.id.mv_password_edit)).getText()
								.toString();
						Config.userName = mUserName;
						Config.userPass = mPassword;
						Config.save();
						Manager.tryLogin(Config.userName, Config.userPass,
								Config.serviceCode);
					}
				}).setNegativeButton(R.string.login_dialog_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		((EditText) loginView.findViewById(R.id.mv_username_edit))
				.setText(Config.userName);
		((EditText) loginView.findViewById(R.id.mv_password_edit))
				.setText(Config.userPass);
		return loginDialog;
	}
	
	static void confirmDialog(String title, DialogInterface.OnClickListener yesListenser) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage(title);
		builder.setTitle("提示");
		builder.setPositiveButton(R.string.dialog_confirm, yesListenser);

		builder.setNegativeButton(R.string.dialog_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

}