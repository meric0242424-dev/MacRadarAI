<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_dark">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Tahmin Geçmişi"
            android:textColor="@color/text_primary"
            android:textSize="22sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp"/>

        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="@color/card_background"
            app:cardCornerRadius="16dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="📊 AI Başarı İstatistikleri"
                    android:textColor="@color/accent_green"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp"/>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="12dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        <TextView
                            android:id="@+id/tvTotalPredictions"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0"
                            android:textColor="@color/text_primary"
                            android:textSize="28sp"
                            android:textStyle="bold"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Toplam"
                            android:textColor="@color/text_secondary"
                            android:textSize="12sp"/>
                    </LinearLayout>

                    <View
                        android:layout_width="1dp"
                        android:layout_height="40dp"
                        android:background="@color/divider_color"/>

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        <TextView
                            android:id="@+id/tvCheckedPredictions"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0"
                            android:textColor="@color/text_primary"
                            android:textSize="28sp"
                            android:textStyle="bold"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Sonuçlanan"
                            android:textColor="@color/text_secondary"
                            android:textSize="12sp"/>
                    </LinearLayout>

                    <View
                        android:layout_width="1dp"
                        android:layout_height="40dp"
                        android:background="@color/divider_color"/>

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        <TextView
                            android:id="@+id/tvWinnerRate"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="-"
                            android:textColor="@color/accent_green"
                            android:textSize="28sp"
                            android:textStyle="bold"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Kazanan %"
                            android:textColor="@color/text_secondary"
                            android:textSize="12sp"/>
                    </LinearLayout>
                </LinearLayout>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Kazanan Tahmini Doğruluğu"
                    android:textColor="@color/text_secondary"
                    android:textSize="12sp"
                    android:layout_marginBottom="4dp"/>
                <ProgressBar
                    android:id="@+id/progressWinnerRate"
                    style="@style/Widget.AppCompat.ProgressBar.Horizontal"
                    android:layout_width="match_parent"
                    android:layout_height="8dp"
                    android:max="100"
                    android:progress="0"
                    android:progressTint="@color/accent_green"
                    android:layout_marginBottom="12dp"/>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                        android:layout_weight="1" android:orientation="vertical" android:gravity="center">
                        <TextView android:id="@+id/tvScoreRate"
                            android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="-" android:textColor="@color/text_primary"
                            android:textSize="18sp" android:textStyle="bold"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="3.5 Üst" android:textColor="@color/text_secondary" android:textSize="11sp"/>
                    </LinearLayout>

                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                        android:layout_weight="1" android:orientation="vertical" android:gravity="center">
                        <TextView android:id="@+id/tvOver25Rate"
                            android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="-" android:textColor="@color/text_primary"
                            android:textSize="18sp" android:textStyle="bold"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="2.5 Üst" android:textColor="@color/text_secondary" android:textSize="11sp"/>
                    </LinearLayout>

                    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                        android:layout_weight="1" android:orientation="vertical" android:gravity="center">
                        <TextView android:id="@+id/tvBttsRate"
                            android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="-" android:textColor="@color/text_primary"
                            android:textSize="18sp" android:textStyle="bold"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                            android:text="KG Var" android:textColor="@color/text_secondary" android:textSize="11sp"/>
                    </LinearLayout>

                </LinearLayout>

            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <TextView
            android:id="@+id/tvEmptyHistory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Henüz tahmin yapılmamış.\nBir maç detayına giderek tahmin oluşturun."
            android:textColor="@color/text_secondary"
            android:textSize="14sp"
            android:gravity="center"
            android:layout_marginTop="32dp"
            android:visibility="gone"/>

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvHistory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:nestedScrollingEnabled="false"/>

    </LinearLayout>

</androidx.core.widget.NestedScrollView>
