/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.controller.zalo;

/**
 *
 * @author HP
 */
public class ZaloConfig {
    // Copy APP ID từ trang Zalo dán vào đây
    public static final String APP_ID = "2534224986155651107"; 
    
    // Copy SECRET KEY từ trang Zalo dán vào đây
    public static final String APP_SECRET = "XVNFc6NnQjYST4B16nir"; 

    // QUAN TRỌNG: Link này phải GIỐNG HỆT link bạn vừa điền trên web Zalo
    public static final String REDIRECT_URI = "https://truongvi-todolist.onrender.com/zalo-callback";
}
