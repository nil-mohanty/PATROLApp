package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.patrol.enums.ClaimType;
import com.example.patrol.utils.UtilityService;
import com.example.patrol.DTO.User;
import com.example.patrol.service.HttpService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText, confirmPasswordEditText;
    private Button submitButton;
    private Spinner claimsDropDown;
    private HttpService httpService;
    private CardView progressCardView;
    private String TAG = "Registration Activity";
    private UtilityService utilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.progressCardView = findViewById(R.id.registration_progress_circular_view);
        this.utilityService = new UtilityService();
        this.emailEditText = findViewById(R.id.editTextRegistrationEmailAddress);
        this.firstNameEditText = findViewById(R.id.editTextFirstName);
        this.lastNameEditText = findViewById(R.id.editTextLastName);
        this.passwordEditText = findViewById(R.id.editTextRegistrationPassword);
        this.confirmPasswordEditText = findViewById(R.id.editTextRegistrationConfirmPassword);
        this.claimsDropDown = findViewById(R.id.dropDownRegistrationClaims);
        this.submitButton = findViewById(R.id.buttonTrendsSubmit);
        this.httpService = HttpService.getInstance();

        validateEmail();
        validatePassword();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.claims, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        claimsDropDown.setAdapter(adapter);

        this.submitButton.setOnClickListener(view -> {
            ClaimType selectedClaim = ClaimType.fromDescription(claimsDropDown.getSelectedItem().toString());
            if(!validateEmptyFields()) {
                showRegistrationFailedMessage("Fill out all the fields");
                return;
            }
            this.progressCardView.setVisibility(View.VISIBLE);
            User user = new User(
                    emailEditText.getText().toString(),
                    firstNameEditText.getText().toString(),
                    lastNameEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    selectedClaim.getCode(),
                    null);

            submitRegistrationRequest(user);
        });
    }

    private boolean validateEmptyFields() {
        String email =  emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        return !email.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !password.isEmpty();
    }
    private void submitRegistrationRequest(User user){
        Gson gson = new Gson();
        String personJson = gson.toJson(user);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ApiResponse response =  this.httpService.createUser(personJson);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (response.getStatusCode() == 201) {
                    Log.d(TAG, "Registration successful: " + response.getResponseBody());
                    redirectToLoginPage("Registration successful. Please log in.");
                } else {
                    Log.d(TAG, "Registration failed: " + response.getResponseBody());
                    showRegistrationFailedMessage("Registration failed. Please try again.");
                }
            });
        });
        executor.shutdown();
        this.progressCardView.setVisibility(View.GONE);
    }

    private void validateEmail(){
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches() && s.length() > 0) {
                    emailEditText.setError("Invalid email address");
                } else {
                    emailEditText.setError(null);
                }
            }
        });
    }

    private void validatePassword(){
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                boolean isValid = utilityService.isValidPassword(password);
                if (isValid) {
                    passwordEditText.setError(null);
                } else {
                    passwordEditText.setError("Password must be at least 8 characters long.");
                }
            }
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordEditText.getText().toString();
                String confirmPassword = s.toString();

                if (password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError(null);
                } else {
                    confirmPasswordEditText.setError("Passwords do not match");
                }
            }
        });
    }

    private void redirectToLoginPage(String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showRegistrationFailedMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
