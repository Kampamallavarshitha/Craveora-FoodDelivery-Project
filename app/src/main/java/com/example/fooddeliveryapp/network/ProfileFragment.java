package com.example.fooddeliveryapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // change to your real support email
    private static final String SUPPORT_EMAIL = "support@fooddeliveryapp.com";

    private TextView txtAvatar, txtName, txtPhone, txtStatOrders, txtStatSpent, txtAddressValue;
    private SwitchCompat switchNotif;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        UiUtils.applyTopInset(view.findViewById(R.id.headerContainer));

        txtAvatar = view.findViewById(R.id.txtAvatar);
        txtName = view.findViewById(R.id.txtProfileName);
        txtPhone = view.findViewById(R.id.txtProfilePhone);
        txtStatOrders = view.findViewById(R.id.txtStatOrders);
        txtStatSpent = view.findViewById(R.id.txtStatSpent);
        txtAddressValue = view.findViewById(R.id.txtAddressValue);
        switchNotif = view.findViewById(R.id.switchNotif);

        view.findViewById(R.id.rowEdit).setOnClickListener(v -> showEditProfile());
        view.findViewById(R.id.rowAddress).setOnClickListener(v -> showEditAddress());
        view.findViewById(R.id.rowNotif).setOnClickListener(v -> switchNotif.toggle());
        view.findViewById(R.id.rowHelp).setOnClickListener(v -> contactSupport());
        view.findViewById(R.id.rowAbout).setOnClickListener(v -> showAbout());
        view.findViewById(R.id.rowClear).setOnClickListener(v -> confirmClearOrders());
        view.findViewById(R.id.rowLogout).setOnClickListener(v -> confirmLogout());

        switchNotif.setOnCheckedChangeListener((b, checked) ->
                AppData.setNotif(requireContext(), checked));

        UiUtils.staggerIn(
                view.findViewById(R.id.headerContainer),
                view.findViewById(R.id.settingsCard));

        refresh();

        return view;
    }

    private void refresh() {

        if (txtName == null) return;

        Context c = requireContext();

        String name = AppData.getName(c);
        String phone = AppData.getPhone(c);

        txtName.setText(name);
        txtPhone.setText(phone.isEmpty() ? "Add your phone number" : phone);
        txtAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.getDefault()));
        txtAddressValue.setText(AppData.getAddress(c));

        switchNotif.setOnCheckedChangeListener(null);
        switchNotif.setChecked(AppData.isNotifOn(c));
        switchNotif.setOnCheckedChangeListener((b, checked) -> AppData.setNotif(requireContext(), checked));

        List<OrderStore.Order> orders = OrderStore.getAll(c);
        int spent = 0;
        for (OrderStore.Order o : orders) spent += o.total;

        txtStatOrders.setText(String.valueOf(orders.size()));
        txtStatSpent.setText("₹" + spent);
    }

    // ---------------- DIALOGS ----------------

    private void showEditProfile() {

        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText etName = dv.findViewById(R.id.etName);
        EditText etPhone = dv.findViewById(R.id.etPhone);

        etName.setText(AppData.getName(requireContext()));
        etPhone.setText(AppData.getPhone(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit profile")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {

                    String n = etName.getText().toString().trim();

                    if (n.isEmpty()) {
                        Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AppData.setName(requireContext(), n);
                    AppData.setPhone(requireContext(), etPhone.getText().toString().trim());
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditAddress() {

        EditText input = new EditText(requireContext());
        input.setText(AppData.getAddress(requireContext()));
        input.setSelection(input.getText().length());
        input.setHint("e.g. Home, Office");

        FrameLayout box = new FrameLayout(requireContext());
        int pad = (int) UiUtils.dp(box, 22);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delivery address label")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String a = input.getText().toString().trim();
                    if (!a.isEmpty()) {
                        AppData.setAddress(requireContext(), a);
                        refresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void contactSupport() {

        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + SUPPORT_EMAIL));
            i.putExtra(Intent.EXTRA_SUBJECT, "Food Delivery App - Help");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAbout() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Food Delivery App")
                .setMessage("Version 1.0\n\nOrder from the best restaurants around you.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmClearOrders() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Clear order history?")
                .setMessage("This removes all past orders from this phone.")
                .setPositiveButton("Clear", (d, w) -> {
                    OrderStore.clear(requireContext());
                    refresh();
                    Toast.makeText(requireContext(), "Order history cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmLogout() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Log out?")
                .setMessage("Your saved profile details will be removed from this phone.")
                .setPositiveButton("Log out", (d, w) -> {

                    AppData.clearProfile(requireContext());
                    refresh();
                    Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();

                    // TODO: if you use Firebase Auth, also call FirebaseAuth.getInstance().signOut()
                    // and start your OTP/login activity here.
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        txtAvatar = null;
        txtName = null;
        txtPhone = null;
        txtStatOrders = null;
        txtStatSpent = null;
        txtAddressValue = null;
        switchNotif = null;
    }
}