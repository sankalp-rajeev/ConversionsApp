package com.example.conversionsapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class UserAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> users;
    private final UserDatabaseHelper dbHelper;

    // Constructor
    public UserAdapter(Context context, List<String> users, UserDatabaseHelper dbHelper) {
        super(context, R.layout.user_list_item, users);
        this.context = context;
        this.users = users;
        this.dbHelper = dbHelper;
    }

    // ViewHolder for optimized view recycling
    private static class ViewHolder {
        TextView textViewUsername;
        Button buttonDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Inflate the layout and set up the ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
            holder = new ViewHolder();
            holder.textViewUsername = convertView.findViewById(R.id.usernameTextView);
            holder.buttonDelete = convertView.findViewById(R.id.deleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the user data for this position
        String username = users.get(position);
        holder.textViewUsername.setText(username);

        // Handle user item click
        convertView.setOnClickListener(v -> {
            // Start UserConversionHistory activity and pass the username
            Intent intent = new Intent(context, UserConversionHistory.class);
            intent.putExtra("username", username);
            context.startActivity(intent);
        });

        // Set delete button functionality
        holder.buttonDelete.setOnClickListener(v -> {
            if ("admin@unitgenie.com".equals(username)) {
                Toast.makeText(context, "Admin account cannot be deleted.", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.deleteUser(username);
                dbHelper.logAdminAction("Deleted user: " + username);
                users.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "User deleted: " + username, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
