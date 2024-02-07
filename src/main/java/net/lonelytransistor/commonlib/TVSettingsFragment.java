package net.lonelytransistor.commonlib;

import android.animation.LayoutTransition;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.VerticalGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TVSettingsFragment extends GuidedStepSupportFragment {
    public TVSettingsFragment() {
        super();
    }

    private final int NEEDS_REBUILDING = 0b11;
    private final int NEEDS_APPENDING = 0b01;
    private int viewState = 0;

    private int index_ActionFocus = 0;

    private final List<ActionHolder> list_ActionHolder = new ArrayList<>();
    private final List<ActionHolder> list_panel_ActionHolder = new ArrayList<>();

    private final List<Action> list_Action = new ArrayList<>();
    private final Map<RadioGroup.Radio, RadioGroup> map_Radio_RadioGroup = new HashMap<>();

    public static class RadioGroup {
        public static class Radio extends Action {
            public Radio setDirty() { return (Radio) super.setDirty(); }
            public Radio setEnable(boolean data) { return (Radio) super.setEnable(data); }
            public Radio setIcon(Drawable data) { return (Radio) super.setIcon(data); }
            public Radio setTitle(String data) { return (Radio) super.setTitle(data); }
            public Radio setDescription(String data) { return (Radio) super.setDescription(data); }

            Object value;
            public Radio setValue(Object data) {
                value = data;
                return this;
            }
            Radio() {}
            Radio(Radio a) {
                super(a);
                value = a.value;
            }
        }
        public interface Callback { void onChanged(List<Radio> group, Radio active, Object value); }
        public Callback onChanged = (g, a, v)->{};
        public RadioGroup(Callback cb) { onChanged = cb; }

        int id;
        List<Radio> items = new ArrayList<>();
        public Radio active;
        public Object value;
        public RadioGroup() {}
        RadioGroup(RadioGroup a) {
            id = a.id;
            onChanged = a.onChanged;
            items = a.items;
            active = a.active;
            value = a.value;
        }

        public Radio get(int ix) {
            return items.get(ix);
        }
        public Radio addItem() {
            Radio action = new Radio();
            items.add(action);
            return action;
        }
    }
    public static class Spinbox extends Action {
        public interface Callback { void onChanged(Spinbox action, double value); }
        public Callback onChanged = (a, v)->{};
        public Spinbox(Callback cb) { onChanged = cb; }

        public Spinbox setDirty() { return (Spinbox) super.setDirty(); }
        public Spinbox setEnable(boolean data) { return (Spinbox) super.setEnable(data); }
        public Spinbox setIcon(Drawable data) { return (Spinbox) super.setIcon(data); }
        public Spinbox setTitle(String data) { return (Spinbox) super.setTitle(data); }
        public Spinbox setDescription(String data) { return (Spinbox) super.setDescription(data); }

        boolean isFloatingPoint() {
            return !(value==(long)value &&
                    minValue==(long)minValue &&
                    maxValue==(long)maxValue &&
                    smallStep==(long)smallStep &&
                    bigStep==(long)bigStep);
        }
        void recalcSteps() {
            boolean round = isFloatingPoint();
            if (defaultSmallStep) {
                smallStep = (maxValue - minValue) / 100;
            }
            if (defaultBigStep) {
                bigStep = (maxValue - minValue) / 10;
            }
            if (!round) {
                smallStep = (long) Math.ceil(smallStep);
                bigStep = (long) Math.ceil(bigStep);
            }
        }

        double value;
        public Spinbox setValue(double data) {
            value = data;
            return setDirty();
        }
        double minValue;
        public Spinbox setMinValue(double data) {
            minValue = data;
            recalcSteps();
            return setDirty();
        }
        double maxValue;
        public Spinbox setMaxValue(double data) {
            maxValue = data;
            recalcSteps();
            return setDirty();
        }

        private boolean defaultSmallStep = true;
        double smallStep = 0;
        public Spinbox setSmallStep(double data) {
            smallStep = data;
            defaultSmallStep = false;
            return setDirty();
        }

        private boolean defaultBigStep = true;
        double bigStep = 0;
        public Spinbox setBigStep(double data) {
            bigStep = data;
            defaultBigStep = false;
            return setDirty();
        }

        public Spinbox() {}
        Spinbox(Spinbox a) {
            super(a);
            onChanged = a.onChanged;
            value = a.value;
            minValue = a.minValue;
            maxValue = a.maxValue;
            defaultSmallStep = a.defaultSmallStep;
            smallStep = a.smallStep;
            defaultBigStep = a.defaultBigStep;
            bigStep = a.bigStep;
        }
    }
    public static class Checkbox extends Action {
        public interface Callback { void onChanged(Checkbox action, boolean value); }
        public Callback onChanged = (a, v)->{};
        public Checkbox(Callback cb) { onChanged = cb; }

        public Checkbox setDirty() { return (Checkbox) super.setDirty(); }
        public Checkbox setEnable(boolean data) { return (Checkbox) super.setEnable(data); }
        public Checkbox setIcon(Drawable data) { return (Checkbox) super.setIcon(data); }
        public Checkbox setTitle(String data) { return (Checkbox) super.setTitle(data); }
        public Checkbox setDescription(String data) { return (Checkbox) super.setDescription(data); }

        boolean value;
        public Checkbox setValue(boolean data) { value = data; return setDirty(); }

        public Checkbox() {}
        Checkbox(Checkbox a) {
            super(a);
            onChanged = a.onChanged;
            value = a.value;
        }
    }
    public static class Editbox extends Action {
        public interface Callback { void onChanged(Editbox action, String value); }
        public Callback onChanged = (a,v)->{};
        public Editbox(Callback cb) { onChanged = cb; }

        public Editbox setDirty() { return (Editbox) super.setDirty(); }
        public Editbox setEnable(boolean data) { return (Editbox) super.setEnable(data); }
        public Editbox setIcon(Drawable data) { return (Editbox) super.setIcon(data); }
        public Editbox setTitle(String data) { return (Editbox) super.setTitle(data); }
        public Editbox setDescription(String data) { return (Editbox) super.setDescription(data); }

        String value;
        public Editbox setValue(String data) { value = data; return setDirty(); }
        String[] autofillHints = null;
        public Editbox setAutofillHints(String[] data) { autofillHints = data; return setDirty(); }

        public Editbox() {}
        Editbox(Editbox a) {
            super(a);
            onChanged = a.onChanged;
            value = a.value;
            autofillHints = a.autofillHints;
        }
    }
    public static class Label extends Action {
        public Label setDirty() { return (Label) super.setDirty(); }
        public Label setFocusable(boolean data) { return (Label) super.setFocusable(data); }
        public Label setEnable(boolean data) { return (Label) super.setEnable(data); }
        public Label setIcon(Drawable data) { return (Label) super.setIcon(data); }
        public Label setTitle(String data) { return (Label) super.setTitle(data); }
        public Label setDescription(String data) { return (Label) super.setDescription(data); }

        boolean focusable = false;
        public Label() {}
        Label(Label a) {
            super(a);
            focusable = false;
        }
    }
    public static class Button extends Action {
        public interface Callback { void onClicked(Button action, Object value); }
        public Button.Callback onClicked = (a, v)->{};
        public Button(Button.Callback cb) { onClicked = cb; }

        public Button setDirty() { return (Button) super.setDirty(); }
        public Button setEnable(boolean data) { return (Button) super.setEnable(data); }
        public Button setIcon(Drawable data) { return (Button) super.setIcon(data); }
        public Button setTitle(String data) { return (Button) super.setTitle(data); }
        public Button setDescription(String data) { return (Button) super.setDescription(data); }

        Object value;
        public Button setValue(Object data) {
            value = data;
            return setDirty();
        }
        public Button() {}
        Button(Button a) {
            super(a);
            onClicked = a.onClicked;
            value = a.value;
        }
    }
    public static class Action {
        Object value;

        List<Action> subActions = new ArrayList<>();
        List<Action> panelActions = new ArrayList<>();

        boolean dirtyPanelActions = false;
        protected Action setDirtyPanelActions() {
            dirtyPanelActions = true;
            return this;
        }
        public Action addPanelAction(@NonNull Action action) {
            panelActions.add(action);
            return setDirtyPanelActions();
        }
        public Action removePanelAction(@NonNull Action action) {
            panelActions.remove(action);
            return setDirtyPanelActions();
        }
        public Action getPanelAction(int ix) {
            return panelActions.get(ix);
        }

        boolean dirtySubActions = false;
        protected Action setDirtySubActions() {
            dirtySubActions = true;
            return this;
        }
        public Action addSubAction(Action action) {
            subActions.add(action);
            return setDirtySubActions();
        }
        public Action removeSubAction(Action action) {
            subActions.remove(action);
            return setDirtySubActions();
        }
        public Action getSubAction(int ix) {
            return subActions.get(ix);
        }

        boolean dirty = false;
        protected Action setDirty() {
            dirty = true;
            return this;
        }
        boolean focusable = true;
        public Action setFocusable(boolean data) { focusable = data; return setDirty(); }
        boolean enabled = true;
        public Action setEnable(boolean data) { enabled = data; return setDirty(); }
        boolean chevron = false;
        public Action setChevron(boolean data) { chevron = data; return setDirty(); }
        Drawable icon = null;
        public Action setIcon(Drawable data) { icon = data; return setDirty(); }
        String title = "";
        public Action setTitle(String data) { title = data; return setDirty(); }
        String description = "";
        public Action setDescription(String data) { description = data; return setDirty(); }

        Action() {}
        Action(Action a) {
            value = a.value;
            enabled = a.enabled;
            focusable = a.focusable;
            chevron = a.chevron;
            icon = a.icon;
            title = a.title;
            description = a.description;
            subActions = a.subActions;
            panelActions = a.panelActions;
        }
    }


    private static class ActionHolder {
        //long id;
        int index;
        Action action;
        GuidedAction builtAction;
        // Only for non-sub actions
        ActionHolder parent = null;
        List<ActionHolder> subActions = new ArrayList<>();
        // Only for non-panel actions and subactions
        boolean isPanelAction = false;
        List<ActionHolder> panelActions = new ArrayList<>();
    }
    private class SpinboxActionHolder extends ActionHolder {
        private View editableView = null;
        public LinearLayout container = null;
        public boolean editing = false;

        private void cleanUp() {
            if (!(container != null && editableView != null))
                return;
            container.removeView(editableView);
        }
        private boolean inject() {
            LinearLayout layout = (LinearLayout) getActionItemView(index);
            if (layout == null)
                return false;
            if (container == layout && editableView != null)
                return true;
            container = layout;

            LayoutInflater inflater = LayoutInflater.from(getContext());
            editableView = inflater.inflate(R.layout.guided_action_number_editable, container, false);

            ViewGroup.LayoutParams params = editableView.getLayoutParams();
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            editableView.setLayoutParams(params);
            container.post(() -> container.addView(editableView));

            return true;
        }
        public void update() {
            if (!inject())
                return;

            Spinbox spinbox = (Spinbox) action;

            spinbox.value = Math.max(spinbox.minValue, Math.min(spinbox.maxValue, spinbox.value));
            TextView valueText = (TextView) editableView.findViewById(R.id.value_text);
            valueText.setText(spinbox.isFloatingPoint() ?
                    String.valueOf(spinbox.value) :
                    String.valueOf((long) spinbox.value));

            ImageView indicator = (ImageView) editableView.findViewById(R.id.number_increase);
            indicator.setVisibility(editing ? View.VISIBLE : View.INVISIBLE);
            indicator.setImageResource(spinbox.value>=spinbox.maxValue ? R.drawable.number_increase_off : R.drawable.number_increase);
            indicator = (ImageView) editableView.findViewById(R.id.number_decrease);
            indicator.setVisibility(editing ? View.VISIBLE : View.INVISIBLE);
            indicator.setImageResource(spinbox.value<=spinbox.minValue ? R.drawable.number_decrease_off : R.drawable.number_decrease);
        }
        public void edit() {
            Spinbox spinbox = (Spinbox) action;

            editing = true;
            View listenerView = getView().findFocus();
            listenerView.setOnKeyListener((View v, int keyCode, KeyEvent event)-> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            spinbox.value -= spinbox.bigStep-spinbox.smallStep;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            spinbox.value -= spinbox.smallStep;
                            update();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            spinbox.value += spinbox.bigStep-spinbox.smallStep;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            spinbox.value += spinbox.smallStep;
                            update();
                            break;
                        case KeyEvent.KEYCODE_ESCAPE:
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_BACK:
                            listenerView.setOnKeyListener(null);
                            finishEditing();
                            break;
                    }
                    double scale = Math.pow(10, 3);
                    spinbox.value = Math.round(spinbox.value * scale) / scale;
                }
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_BACK:
                        return true;
                }
                return false;
            });
            update();
        }
        private void finishEditing() {
            Spinbox spinbox = (Spinbox) action;

            editing = false;
            spinbox.onChanged.onChanged(spinbox, spinbox.value);

            transferAction(builtAction, action);
            update();

            if (isPanelAction) {
                notifyButtonActionChanged(index);
            } else {
                notifyActionChanged(index);
            }
        }
    }


    private void transferAction(GuidedAction guidedAction, Action action) {
        guidedAction.setTitle(action.title);
        guidedAction.setDescription(action.description);
        guidedAction.setIcon(action.icon);
        guidedAction.setEnabled(action.enabled);
        guidedAction.setFocusable(action.focusable);
        if (action instanceof Checkbox) {
            guidedAction.setChecked(((Checkbox) action).value);
        } else if (action instanceof Editbox) {
            guidedAction.setEditTitle(((Editbox) action).value);
        } else if (action instanceof RadioGroup.Radio) {
            RadioGroup group = map_Radio_RadioGroup.get((RadioGroup.Radio) action);
            guidedAction.setChecked(group.active == action);
        }
        action.dirty = false;
    }
    private synchronized GuidedAction buildGuidedAction(Action action, List<GuidedAction> subActions, boolean isPanelAction) {
        GuidedAction.Builder builder = new GuidedAction.Builder(getContext())
                .hasNext(action.chevron)
                .id(isPanelAction ? -(list_panel_ActionHolder.size()+1) : (list_ActionHolder.size()+1))
                .subActions(subActions.size()>0 ? subActions : null);
        if (action instanceof Label) {
            builder.infoOnly(true);
        } else if (action instanceof Button) {
        } else if (action instanceof Checkbox) {
            builder.checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID);
        } else if (action instanceof Editbox) {
            builder.editable(true);
            builder.autofillHints(((Editbox) action).autofillHints);
        } else if (action instanceof RadioGroup.Radio) {
            RadioGroup.Radio actionI = (RadioGroup.Radio) action;
            RadioGroup group = map_Radio_RadioGroup.get(actionI);
            builder.checked(group.active == actionI)
                    .checkSetId(group.id);
        }
        GuidedAction guidedAction = builder.build();
        transferAction(guidedAction, action);
        return guidedAction;
    }
    private ActionHolder buildAction(Action action) {
        return buildAction(action, false);
    }
    private ActionHolder buildAction(Action action, boolean isPanelAction) {
        return buildAction(action, isPanelAction, false);
    }
    synchronized private ActionHolder buildAction(Action action, boolean isPanelAction, boolean isSubAction) {
        ActionHolder holder;
        if (action instanceof Spinbox) {
            holder = new SpinboxActionHolder();
        } else {
            holder = new ActionHolder();
        }

        List<GuidedAction> subActions = new ArrayList<>();
        if (!isSubAction && action.subActions != null && action.subActions.size() > 0) {
            for (Action subAction : action.subActions) {
                ActionHolder subHolder = buildAction(subAction, isPanelAction, true);

                subHolder.parent = holder;
                subActions.add(subHolder.builtAction);
                holder.subActions.add(subHolder);
            }
        }

        holder.builtAction = buildGuidedAction(action, subActions, isPanelAction);
        holder.index = (int) Math.abs(holder.builtAction.getId())-1;
        holder.action = action;
        holder.isPanelAction = isPanelAction;

        if (isPanelAction) {
            list_panel_ActionHolder.add(holder);
        } else {
            list_ActionHolder.add(holder);
        }
        return holder;
    }
    private void buildPanelActions(int ix) {
        list_panel_ActionHolder.clear();
        List<GuidedAction> actions = new ArrayList<>();
        if (ix < list_ActionHolder.size()) {
            for (Action action : list_ActionHolder.get(ix).action.panelActions) {
                actions.add(buildAction(action, true).builtAction);
            }
        }
        ViewGroup.LayoutParams layoutParams = hierarchy.actions.getLayoutParams();
        if (actions.size() == 0 && layoutParams.width == 0) {
            layoutParams.width = hierarchy.actions.getWidth() + hierarchy.panel.getWidth();
            hierarchy.actions.setLayoutParams(layoutParams);
        } else if (actions.size() > 0 && layoutParams.width != 0) {
            layoutParams.width = 0;
            hierarchy.actions.setLayoutParams(layoutParams);
        }
        setButtonActions(actions);
        if (actions.size() > 0) {
            getGuidedButtonActionsStylist().getActionsGridView().setSelectedPositionSmooth(0);
        }
    }


    @Override
    public void onGuidedActionFocused(GuidedAction action) {
        if (action != null) {
            long id = action.getId();
            int index = (int) id-1;
            if (id > 0 && index != index_ActionFocus) {
                index_ActionFocus = index;
                buildPanelActions(index);
            }
        } else {
            buildPanelActions(0);
        }
    }
    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction guidedAction) {
        long id = guidedAction.getId();
        int index = Math.abs((int) id) - 1;
        ActionHolder actionHolder = id > 0 ? list_ActionHolder.get(index) : list_panel_ActionHolder.get(index);
        if (actionHolder==null || !(actionHolder.action instanceof Editbox))
            return GuidedAction.ACTION_ID_CURRENT;

        Editbox action = (Editbox) actionHolder.action;
        action.value = guidedAction.getEditTitle().toString();

        action.onChanged.onChanged(action, action.value);

        if (actionHolder.action.dirty) {
            transferAction(guidedAction, actionHolder.action);
            if (id > 0) {
                notifyActionChanged(index);
            } else {
                notifyButtonActionChanged(index);
            }
        }
        return GuidedAction.ACTION_ID_CURRENT;
    }
    @Override
    public void onGuidedActionClicked(GuidedAction guidedAction) {
        long id = guidedAction.getId();
        int index = Math.abs((int) id) - 1;
        ActionHolder actionHolder = id > 0 ? list_ActionHolder.get(index) : list_panel_ActionHolder.get(index);
        if (actionHolder == null)
            return;

        if (actionHolder.action instanceof Button) {
            Button action = (Button) actionHolder.action;

            action.onClicked.onClicked(action, action.value);
        } else if (actionHolder.action instanceof Spinbox) {
            SpinboxActionHolder holder = (SpinboxActionHolder) actionHolder;
            holder.edit();
        } else if (actionHolder.action instanceof Checkbox) {
            Checkbox action = (Checkbox) actionHolder.action;
            action.value = guidedAction.isChecked();

            action.onChanged.onChanged(action, action.value);
        } else if (actionHolder.action instanceof RadioGroup.Radio) {
            RadioGroup.Radio action = (RadioGroup.Radio) actionHolder.action;
            RadioGroup group = map_Radio_RadioGroup.get(action);
            group.active = action;

            group.onChanged.onChanged(group.items, action, action.value);
        }
        update();
    }
    public void update() {
        for (ActionHolder holder : list_ActionHolder) {
            if (holder.action.dirty) {
                transferAction(holder.builtAction, holder.action);
                notifyActionChanged(holder.index);
            }
        }
        for (ActionHolder holder : list_panel_ActionHolder) {
            if (holder.action.dirty) {
                transferAction(holder.builtAction, holder.action);
                notifyButtonActionChanged(holder.index);
            }
        }
        updateHolders();
    }
    private void updateHolders() {
        if ((viewState & NEEDS_REBUILDING) > 0) {
            list_ActionHolder.clear();
        }
        if ((viewState & NEEDS_APPENDING) > 0) {
            List<GuidedAction> actions = new ArrayList<>();
            for (ActionHolder action : list_ActionHolder) {
                actions.add(action.builtAction);
            }
            for (int ix=list_ActionHolder.size(); ix<list_Action.size(); ix++) {
                actions.add(buildAction(list_Action.get(ix)).builtAction);
            }
            setActions(actions);
            viewState = 0;
        }
    }

    private final RootContainers hierarchy = new RootContainers();
    private static class RootContainers {
        LinearLayout container;

        ViewGroup banner;

        LinearLayout actionsPanelContainer;
        ViewGroup actions;
        ViewGroup panel;
    }
    private static class RootContainerHier {
        ViewGroup top;
        ViewGroup topL;
        ViewGroup topR;
    }
    private RootContainerHier getCommonRootContainer(View l, View r) {
        RootContainerHier ret = new RootContainerHier();

        List<View> parentsActions = new ArrayList<>();

        ViewParent parent = l.getParent();
        while (parent instanceof ViewGroup) {
            parentsActions.add((View) parent);
            parent = parent.getParent();
        }

        parent = r.getParent();
        while (parent instanceof ViewGroup && !parentsActions.contains(parent)) {
            ret.topR = (ViewGroup) parent;
            parent = parent.getParent();
        }

        assert(parent instanceof ViewGroup);
        ret.top = (ViewGroup) parent;
        for (int ix=0; ix<ret.top.getChildCount(); ix++) {
            if (parentsActions.contains(ret.top.getChildAt(ix))) {
                ret.topL = (ViewGroup) ret.top.getChildAt(ix);
                break;
            }
        }
        return ret;
    }
    private void setEqualWeights(LinearLayout layout) {
        setEqualWeights(layout, 1.0f);
    }
    private void setEqualWeights(LinearLayout layout, float firstMult) {
        int count = layout.getChildCount();
        layout.setWeightSum(count + (firstMult-1.0f));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layout.setLayoutTransition(layoutTransition);

        LinearLayout.LayoutParams params;
        for (int ix=0; ix<count; ix++) {
            View view = layout.getChildAt(ix);
            params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.weight = (ix == 0) ? firstMult : 1.0f;
            view.setLayoutParams(params);
        }
    }
    private void setEqualWeights() {
        RootContainerHier data = getCommonRootContainer(
                getGuidedActionsStylist().getActionsGridView(),
                getGuidedButtonActionsStylist().getActionsGridView());
        hierarchy.actionsPanelContainer = (LinearLayout) data.top;
        hierarchy.actions = data.topL;
        hierarchy.panel = data.topR;
        data = getCommonRootContainer(
                getGuidedActionsStylist().getActionsGridView(),
                getGuidanceStylist().getIconView());
        hierarchy.container = (LinearLayout) data.top;
        hierarchy.banner = data.topR;

        if (hierarchy.actionsPanelContainer != null) {
            setEqualWeights(hierarchy.actionsPanelContainer);
        }
        if (hierarchy.container != null) {
            setEqualWeights(hierarchy.container, 0.85f);
        }
    }


    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(title, description, subtitle, icon == null ?
                AppCompatResources.getDrawable(getActivity(), R.drawable.settings) : icon);
    }
    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        super.onCreateButtonActions(actions, savedInstanceState);
        if (recreating)
            return;
        actions.add(new GuidedAction.Builder(getContext()).title("dummy").build());
    }
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        super.onCreateButtonActions(actions, savedInstanceState);
        if (recreating)
            return;
        actions.add(new GuidedAction.Builder(getContext()).title("dummy").build());
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }


    public Drawable getDrawable(int ref) {
        return AppCompatResources.getDrawable(getActivity(), ref);
    }
    public Action addAction(@NonNull Action action) {
        viewState |= NEEDS_APPENDING;

        list_Action.add(action);
        return action;
    }
    public RadioGroup addAction(@NonNull RadioGroup group) {
        viewState |= NEEDS_APPENDING;

        group.id = map_Radio_RadioGroup.size()+1;
        for (RadioGroup.Radio radio : group.items) {
            list_Action.add(radio);
            map_Radio_RadioGroup.put(radio, group);
        }
        return group;
    }
    private void addPanelActionRadio(@NonNull Action parent, @NonNull RadioGroup group) {
        group.id = map_Radio_RadioGroup.size()+1;
        for (RadioGroup.Radio radio : group.items) {
            parent.panelActions.add(radio);
            map_Radio_RadioGroup.put(radio, group);
        }
    }
    public Action addPanelAction(@NonNull Action parent, @NonNull Object... actions) {
        for (Object action : actions) {
            if (action instanceof RadioGroup) {
                addPanelActionRadio(parent, (RadioGroup) action);
            } else {
                parent.addPanelAction((Action) action);
            }
        }
        return parent.setDirtyPanelActions();
    }
    private void removePanelActionRadio(@NonNull Action parent, @NonNull RadioGroup group) {
        for (RadioGroup.Radio radio : group.items) {
            parent.panelActions.remove(radio);
            map_Radio_RadioGroup.remove(radio, group);
        }
    }
    public Action removePanelAction(@NonNull Action parent, @NonNull Object... actions) {
        for (Object action : actions) {
            if (action instanceof RadioGroup) {
                removePanelActionRadio(parent, (RadioGroup) action);
            } else {
                parent.panelActions.remove((Action) action);
            }
        }
        return parent.setDirtyPanelActions();
    }
    public void removeAction(@NonNull Action action) {
        if (!list_Action.contains(action))
            return;
        viewState |= NEEDS_REBUILDING;

        if (action instanceof Spinbox) {
            ((SpinboxActionHolder) list_ActionHolder.get(
                    list_Action.indexOf(action))).cleanUp();
        }
        list_Action.remove(action);
    }
    public void removeAction(@NonNull RadioGroup group) {
        if (!list_Action.contains(group))
            return;
        viewState |= NEEDS_REBUILDING;

        for (RadioGroup.Radio radio : group.items) {
            list_Action.remove(radio);
            map_Radio_RadioGroup.remove(radio, group);
        }
    }
    private String title = "title";
    private String description = "title";
    private String subtitle = "title";
    private Drawable icon;
    public void setTitle(int data) {
        getGuidanceStylist().getTitleView().setText(data);
        title = getGuidanceStylist().getTitleView().getText().toString();
    }
    public void setTitle(String data) {
        getGuidanceStylist().getTitleView().setText(data);
        title = getGuidanceStylist().getTitleView().getText().toString();
    }
    public void setDescription(int data) {
        getGuidanceStylist().getDescriptionView().setText(data);
        description = getGuidanceStylist().getDescriptionView().getText().toString();
    }
    public void setDescription(String data) {
        getGuidanceStylist().getDescriptionView().setText(data);
        description = getGuidanceStylist().getDescriptionView().getText().toString();
    }
    public void setSubtitle(int data) {
        getGuidanceStylist().getBreadcrumbView().setText(data);
        subtitle = getGuidanceStylist().getBreadcrumbView().getText().toString();
    }
    public void setSubtitle(String data) {
        getGuidanceStylist().getBreadcrumbView().setText(data);
        subtitle = getGuidanceStylist().getBreadcrumbView().getText().toString();
    }
    public void setIcon(int data) {
        getGuidanceStylist().getIconView().setImageResource(data);
        icon = getGuidanceStylist().getIconView().getDrawable();
    }
    public void setIcon(Drawable data) {
        getGuidanceStylist().getIconView().setImageDrawable(data);
        icon = getGuidanceStylist().getIconView().getDrawable();
    }
    public abstract void onBuild(View view);

    private boolean recreating = false;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        VerticalGridView parent = getGuidedActionsStylist().getActionsGridView();
        parent.setOnChildLaidOutListener((parent1, view1, position, id) -> {
            if (position < list_ActionHolder.size() && list_ActionHolder.get(position) instanceof SpinboxActionHolder) {
                ((SpinboxActionHolder) list_ActionHolder.get(position)).update();
            }
        });
        parent.getViewTreeObserver().addOnPreDrawListener(() -> {
            updateHolders();
            return true;
        });

        ImageView iconView = getGuidanceStylist().getIconView();
        ViewGroup.LayoutParams params = iconView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        iconView.setLayoutParams(params);

        setEqualWeights();

        view.post(()->buildPanelActions(index_ActionFocus));
        if (recreating)
            return;

        recreating = true;
        setActions(new ArrayList<>());
        setButtonActions(new ArrayList<>());
        onBuild(view);
    }
}
