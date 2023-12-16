package net.lonelytransistor.commonlib.pairing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import net.lonelytransistor.commonlib.R;

import java.text.DateFormat;
import java.util.Date;

public class Devices {
    /*private String getString(boolean b, int t, int f) {
        return mContext.getResources().getString(b ? t : f);
    }
    public class DevicePresenter extends Presenter {
        DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        @Override
        public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View deviceView = inflater.inflate(R.layout.mainactivity_device_card, parent, false);
            return new ViewHolder(deviceView);
        }
        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            DeviceData data = (DeviceData)item;
            View itemView = viewHolder.view;

            ((ImageView) itemView.findViewById(R.id.deviceLogo)).
                setImageDrawable(
                    AppCompatResources.getDrawable(
                        mContext,
                        getLogo(data.type)
                    )
                );
            ((TextView) itemView.findViewById(R.id.deviceName)).
                setText(
                    data.name
                );
            ((TextView) itemView.findViewById(R.id.devicePaired)).
                    setText(
                            String.format(
                                    mContext.getResources().getString(R.string.paired_on),
                                    mDateFormat.format(data.datePaired)
                            )
                    );
            ((TextView) itemView.findViewById(R.id.deviceActive)).
                    setText(
                            String.format(
                                    mContext.getResources().getString(R.string.last_active_on),
                                    mDateFormat.format(data.lastActive)
                            )
                    );
            itemView.setOnClickListener((view) -> {
                PromptModal.DialogButton[] btns = {
                    new PromptModal.DialogButton("Unpair", PromptModal.ButtonBg.RED, (v) -> {
                        mDevices.remove(data);
                        saveDevices();
                        return true;
                    }),
                    new PromptModal.DialogButton(getString(data.isRestricted(), R.string.unrestrict, R.string.restrict), PromptModal.ButtonBg.YELLOW, (v) -> {
                        ((TextView) v).setText(getString(data.restrict(!data.isRestricted()), R.string.unrestrict, R.string.restrict));
                        saveDevices();
                        return false;
                    }),
                    new PromptModal.DialogButton(getString(data.isMuted(), R.string.unmute, R.string.mute), PromptModal.ButtonBg.YELLOW, (v) -> {
                        ((TextView) v).setText(getString(data.mute(!data.isMuted()), R.string.unmute, R.string.mute));
                        saveDevices();
                        return false;
                    }),
                    new PromptModal.DialogButton("Close", PromptModal.ButtonBg.NORMAL, (v) -> {
                        saveDevices();
                        return true;
                    })
                };
                mModal.show(
                    getLogo(data.type),
                    data.name,
                    getString(true, R.string.device_options_desc,0),
                    btns
                );
            });
        }
        @Override
        public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

        }
    }
    private ArrayObjectAdapter mDevices;
    private final FragmentActivity mContext;
    private final PromptModal mModal;
    public DeviceData[] getDevices() {
        return mDevices.unmodifiableList().toArray(new DeviceData[0]);
    }
    private void add(DeviceData dev) {
        mDevices.add(dev);
    }
    private void loadDevices() {
        for (PairedDevices.DeviceData dev : DataStore.readPairedDevices()) {
            add(dev);
        }
    }
    private void saveDevices() {
        DataStore.savePairedDevices(getDevices());
    }

    Devices(FragmentActivity ctx, VerticalGridView view) {
        mModal = new PromptModal(ctx);
        mContext = ctx;
        mDevices = new ArrayObjectAdapter(new DevicePresenter());
        loadDevices();
        mDevices.add(new DeviceData(RemoteDeviceType.MOBILE,"dupa",new Date(),new Date(),"MG546LNIOA",0));
        saveDevices();
        view.setAdapter(new ItemBridgeAdapter(mDevices));
    }*/
}
