package com.MagicDevelopers.buzzup.HOME.Fragments.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.LOGIN.LoginActivity;
import com.MagicDevelopers.buzzup.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout optionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        optionsContainer = findViewById(R.id.optionsContainer);

        // Botón volver
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // Opciones
        addOption(R.drawable.ic_account, "Cuenta");
        addOption(R.drawable.ic_notifications, "Notificaciones");
        addOption(R.drawable.ic_privacy, "Privacidad");
        addOption(R.drawable.ic_settings, "Configuración avanzada");
        addOption(R.drawable.ic_info, "Acerca de");
        addOption(R.drawable.ic_logout, "Cerrar sesión");
    }

    private void addOption(int iconResId, String title) {
        View optionView = LayoutInflater.from(this)
                .inflate(R.layout.item_setting_option, optionsContainer, false);

        ImageView icon = optionView.findViewById(R.id.icon);
        TextView text = optionView.findViewById(R.id.title);

        icon.setImageResource(iconResId);
        text.setText(title);

        optionView.setOnClickListener(v -> handleOptionClick(title));

        optionsContainer.addView(optionView);
    }

    private void handleOptionClick(String title) {
        switch (title) {
            case "Cuenta":
                startActivity(new Intent(this, AccountSettingsActivity.class));
                break;
            case "Notificaciones":
                Toast.makeText(this, "Aquí puedes abrir la configuración de notificaciones.", Toast.LENGTH_SHORT).show();
                break;
            case "Privacidad":
                Toast.makeText(this, "Aquí puedes abrir la configuración de privacidad.", Toast.LENGTH_SHORT).show();
                break;
            case "Configuración avanzada":
                Toast.makeText(this, "Aquí puedes abrir la configuración avanzada.", Toast.LENGTH_SHORT).show();
                break;
            case "Acerca de":
                Toast.makeText(this, "Aplicación BuzzUp versión 1.0", Toast.LENGTH_SHORT).show();
                break;
            case "Cerrar sesión":
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            default:
                Toast.makeText(this, "Opción no implementada.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
