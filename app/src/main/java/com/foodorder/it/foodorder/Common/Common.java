package com.foodorder.it.foodorder.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.foodorder.it.foodorder.Model.User;
import com.foodorder.it.foodorder.Remote.APIService;
import com.foodorder.it.foodorder.Remote.RetrofitClient;

import retrofit2.Call;

public class Common {

public static User CurrentUser;

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static final String BASE_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "shipped";
    }
    public static Boolean isConnectToTheInternet (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager !=null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info !=null)
            {
                for (int i= 0 ;i<info.length;i++)
                {
                    if(info[i].getState()== NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }



}
