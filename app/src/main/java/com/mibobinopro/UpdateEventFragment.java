package com.mibobinopro;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.mibobinopro.modals.AnimalEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UpdateEventFragment extends Fragment {

    private EditText eventDateEditText;
    private EditText animalIdEditText;
    private AutoCompleteTextView eventNameEditText;
    private EditText stockBullEditText;
    private EditText notesEditText;
    private Button updateBtn;
    private Button resetBtn;

    // CONEXIÓN A FIRESTORE
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventsCollection = db.collection("events");

    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            date = bundle.getString("eventDate", ""); // Clave, valor predeterminado
        }
        return inflater.inflate(R.layout.fragment_update_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventDateEditText = view.findViewById(R.id.eventDateEditText);
        animalIdEditText = view.findViewById(R.id.AnimalIdEditText);
        eventNameEditText = view.findViewById(R.id.eventNameEditText);
        stockBullEditText = view.findViewById(R.id.stockBullEditText);
        notesEditText = view.findViewById(R.id.notesEditText);
        updateBtn = view.findViewById(R.id.updateBtn);
        resetBtn = view.findViewById(R.id.resetBtn);

        eventNameEditText.setThreshold(2);

        final String[] events = new String[] {"Detección de Calor", "Servicios", "Chequeo de Peso"};

        ArrayAdapter<String> eventNameAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, events);
        eventNameEditText.setAdapter(eventNameAdapter);

        eventNameEditText.setOnTouchListener((v, event) -> {
            eventNameEditText.showDropDown();
            return true;
        });

        eventDateEditText.setOnTouchListener((v, event) -> {
            Calendar mCalendar = new GregorianCalendar();
            mCalendar.setTime(new Date());

            new DatePickerDialog(getActivity(), R.style.my_dialog_theme, (view1, year, month, dayOfMonth) -> {
                eventDateEditText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
            }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            return true;
        });

        resetBtn.setOnClickListener(v -> {
            eventDateEditText.setText("");
            animalIdEditText.setText("");
            eventNameEditText.setText("");
            stockBullEditText.setText("");
            notesEditText.setText("");
        });

        updateBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(eventDateEditText.getText().toString().trim())) {
                TastyToast.makeText(getActivity(), "¡Por favor, ingresa la fecha del evento!", TastyToast.LENGTH_LONG, TastyToast.ERROR);
            } else if (TextUtils.isEmpty(animalIdEditText.getText().toString().trim())) {
                TastyToast.makeText(getActivity(), "¡Por favor, ingresa el ID del animal!", TastyToast.LENGTH_LONG, TastyToast.ERROR);
            } else if (TextUtils.isEmpty(eventNameEditText.getText().toString().trim())) {
                TastyToast.makeText(getActivity(), "¡Por favor, ingresa el nombre del evento!", TastyToast.LENGTH_LONG, TastyToast.ERROR);
            } else {
                String date = eventDateEditText.getText().toString().trim();
                String animalId = animalIdEditText.getText().toString().trim();
                String eventName = eventNameEditText.getText().toString().trim();
                String stockBull = stockBullEditText.getText().toString().trim();
                String note = notesEditText.getText().toString().trim();

                deleteEvent(animalId, eventName, date, stockBull, note);
            }
        });

        if (!TextUtils.isEmpty(date)) {
            getEvent(date);
        }
    }

    private void updateEvent(String animalId, String eventName, String date, String stockBull, String note) {
        AnimalEvent event = new AnimalEvent();
        event.setAnimalId(animalId);
        event.setEventDate(date);
        event.setEventName(eventName);
        event.setStockBull(stockBull);
        event.setNotes(note);

        db.collection("events").document(date).set(event)
                .addOnSuccessListener(aVoid -> {
                    eventDateEditText.setText("");
                    animalIdEditText.setText("");
                    eventNameEditText.setText("");
                    stockBullEditText.setText("");
                    notesEditText.setText("");

                    AnimalEventFragment animalEventFragment = new AnimalEventFragment();
                    getFragmentManager().beginTransaction().replace(R.id.container, animalEventFragment).commit();
                })
                .addOnFailureListener(e -> TastyToast.makeText(getActivity(), "Ha ocurrido un error. Inténtalo de nuevo.", TastyToast.LENGTH_LONG, TastyToast.ERROR));
    }

    private void getEvent(String date) {
        eventsCollection.whereEqualTo("eventDate", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot events : queryDocumentSnapshots) {
                            AnimalEvent event = events.toObject(AnimalEvent.class);
                            eventDateEditText.setText(event.getEventDate());
                            animalIdEditText.setText(event.getAnimalId());
                            eventNameEditText.setText(event.getEventName());
                            stockBullEditText.setText(event.getStockBull());
                            notesEditText.setText(event.getNotes());
                        }
                    } else {
                        TastyToast.makeText(getActivity(), "No se encontró ningún evento para esta fecha.", TastyToast.LENGTH_LONG, TastyToast.INFO);
                    }
                })
                .addOnFailureListener(e -> TastyToast.makeText(getActivity(), "Error al recuperar el evento. Inténtalo de nuevo.", TastyToast.LENGTH_LONG, TastyToast.ERROR));
    }

    private void deleteEvent(String animalId, String eventName, String date, String stockBull, String note) {
        eventsCollection.document(date)
                .delete()
                .addOnCompleteListener(task -> updateEvent(animalId, eventName, date, stockBull, note))
                .addOnFailureListener(e -> TastyToast.makeText(getActivity(), "Error al eliminar el evento. Inténtalo de nuevo.", TastyToast.LENGTH_LONG, TastyToast.ERROR));
    }
}
