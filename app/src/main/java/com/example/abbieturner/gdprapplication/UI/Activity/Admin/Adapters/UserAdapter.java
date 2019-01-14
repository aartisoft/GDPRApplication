package com.example.abbieturner.gdprapplication.UI.Activity.Admin.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.abbieturner.gdprapplication.Models.User;
import com.example.abbieturner.gdprapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.UserHolder> {

    private final UserClickListener listener;

    public UserAdapter(@NonNull FirebaseRecyclerOptions<User> options, UserClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User model) {

        holder.bind(model);
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.employee_item, viewGroup, false);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private User user;
        TextView name;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
        }

        public void bind(User model) {
            user = model;
            name.setText(user.getName());
        }

        @Override
        public void onClick(View v) {
            listener.onEmployeeItemClick(user);
        }
    }

    public interface UserClickListener {
        void onEmployeeItemClick(User user);
    }

}