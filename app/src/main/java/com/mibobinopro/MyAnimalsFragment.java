package com.mibobinopro;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mibobinopro.UI.AnimalListRecyclerAdapter;
import com.mibobinopro.helper.CommonConstants;
import com.mibobinopro.modals.Animal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.sdsmdg.tastytoast.TastyToast;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MyAnimalsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference animalsCollection = db.collection("animals");

    private RecyclerView recyclerview;
    private AnimalListRecyclerAdapter animalListAdapter;

    private List<Animal> animalsList;
    private ItemTouchHelper.SimpleCallback simpleCallback;
    private Animal deletedAnimal;
    private int position;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.my_animals_fragment,container,false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        animalsList = new ArrayList<>();
        recyclerview = view.findViewById(R.id.animalListRecyclerView);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        getAnimalList();

        simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @SuppressLint({"ResourceAsColor", "SupportAnnotationUsage"})
            @ColorRes
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String animalId = animalsList.get(position).getAnimalId();
                deletedAnimal = animalsList.get(position);

                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        FancyAlertDialog.Builder
                                .with(getActivity())
                                .setTitle("¡Advertencia!")
                                .setBackgroundColor(Color.parseColor("#ff0000"))  // for @ColorRes use setBackgroundColorRes(R.color.colorvalue)
                                .setMessage("¿Quieres eliminar este animal?")
                                .setNegativeBtnText("Eliminar")
                                .setPositiveBtnBackground( Color.parseColor("#F57C00"))  // for @ColorRes use setPositiveBtnBackgroundRes(R.color.colorvalue)
                                .setPositiveBtnText("Cancelar")
                                .setNegativeBtnBackground(R.color.negativeBtn)  // for @ColorRes use setNegativeBtnBackgroundRes(R.color.colorvalue)
                                .setAnimation(Animation.POP)
                                .isCancellable(true)
                                .setIcon(R.drawable.ic_baseline_pan_tool_24, View.VISIBLE)
                                .onPositiveClicked(dialog -> {
                                    MyAnimalsFragment myAnimalsFragment = new MyAnimalsFragment();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.container, myAnimalsFragment)
                                            .commit();
                                })
                                .onNegativeClicked(dialog -> {
                                    animalsList.remove(position);
                                    animalListAdapter.notifyItemRemoved(position);
                                    removeAnimal(animalId);
                                })
                                .build()
                                .show();
                        break;
                    case ItemTouchHelper.RIGHT:
                        UpdateAnimalFragment updateAnimalFragment = new UpdateAnimalFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("animalId", animalId);
                        updateAnimalFragment.setArguments(bundle);
                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, updateAnimalFragment)
                                .commit();
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(),R.color.positiveBtn))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(),R.color.green))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

    }

    private void removeAnimal(String animalId) {
        animalsCollection.document(animalId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(recyclerview,getString(R.string.wantUnDo),Snackbar.LENGTH_LONG)
                                .setAction("Deshacer", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        animalsCollection.document(deletedAnimal.getAnimalId()).set(deletedAnimal);
                                        animalsList.add(position,deletedAnimal);
                                        animalListAdapter.notifyItemInserted(position);
                                    }
                                }).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getAnimalList(){
        animalsCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(QueryDocumentSnapshot animals : queryDocumentSnapshots){
                                Animal animal = animals.toObject(Animal.class);
                                animalsList.add(animal);
                            }
                            animalListAdapter = new AnimalListRecyclerAdapter(getActivity(),animalsList);
                            recyclerview.setAdapter(animalListAdapter);
                            animalListAdapter.notifyDataSetChanged();

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
                                e.getLocalizedMessage(),
                                TastyToast.LENGTH_LONG,
                                TastyToast.INFO
                        );
                    }
                });
    }

}
