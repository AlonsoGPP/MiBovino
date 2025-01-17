package com.mibobinopro;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mibobinopro.UI.MedicineListRecyclerAdapter;
import com.mibobinopro.modals.Medicine;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sdsmdg.tastytoast.TastyToast;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MedicalCabinetFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference medicineCollection = db.collection("medicine");

    private RecyclerView recyclerview;
    private MedicineListRecyclerAdapter medicineListAdapter;

    private List<Medicine> medicineList;
    private ItemTouchHelper.SimpleCallback simpleCallback;
    private Medicine deletedMedicine;
    private int position;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.medical_cabinet_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        medicineList = new ArrayList<>();
        recyclerview = view.findViewById(R.id.medicineListRecyclerView);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        getMedicineList();

        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String name = medicineList.get(position).getName();
                deletedMedicine = medicineList.get(position);

                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        FancyAlertDialog.Builder
                                .with(getActivity())
                                .setTitle("¡Advertencia!")
                                .setBackgroundColor(Color.parseColor("#ff0000"))
                                .setMessage("¿Desea eliminar este medicamento?")
                                .setNegativeBtnText("Eliminar")
                                .setPositiveBtnBackground(Color.parseColor("#00912B"))
                                .setPositiveBtnText("Cancelar")
                                .setNegativeBtnBackground(Color.parseColor("#00912B"))
                                .setAnimation(Animation.POP)
                                .isCancellable(true)
                                .setIcon(R.drawable.ic_baseline_pan_tool_24, View.VISIBLE)
                                .onPositiveClicked(dialog -> {
                                    MedicalCabinetFragment medicalCabinetFragment = new MedicalCabinetFragment();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.container, medicalCabinetFragment)
                                            .commit();
                                })
                                .onNegativeClicked(dialog -> {
                                    medicineList.remove(position);
                                    medicineListAdapter.notifyItemRemoved(position);
                                    removeMedicine(name);
                                })
                                .build()
                                .show();
                        break;
                    case ItemTouchHelper.RIGHT:
                        UpdateMedicalFragment updateMedicalFragment = new UpdateMedicalFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("name", name);
                        updateMedicalFragment.setArguments(bundle);
                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, updateMedicalFragment)
                                .commit();
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
    }

    private void removeMedicine(String name) {
        medicineCollection.document(name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(recyclerview, "¿Desea deshacer la eliminación?", Snackbar.LENGTH_LONG)
                                .setAction("Deshacer", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        medicineCollection.document(deletedMedicine.getName()).set(deletedMedicine);
                                        medicineList.add(position, deletedMedicine);
                                        medicineListAdapter.notifyItemInserted(position);
                                    }
                                }).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        TastyToast.makeText(
                                getActivity(),
                                "Error al eliminar el medicamento: " + e.getLocalizedMessage(),
                                TastyToast.LENGTH_LONG,
                                TastyToast.ERROR
                        );
                    }
                });
    }

    private void getMedicineList() {
        medicineCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot medines : queryDocumentSnapshots) {
                                Medicine medicine = medines.toObject(Medicine.class);
                                medicineList.add(medicine);
                            }

                            medicineListAdapter = new MedicineListRecyclerAdapter(getActivity(), medicineList);
                            recyclerview.setAdapter(medicineListAdapter);
                            medicineListAdapter.notifyDataSetChanged();

                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                            itemTouchHelper.attachToRecyclerView(recyclerview);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        TastyToast.makeText(
                                getActivity(),
                                "Error al obtener la lista de medicamentos: " + e.getLocalizedMessage(),
                                TastyToast.LENGTH_LONG,
                                TastyToast.ERROR
                        );
                    }
                });
    }
}
