<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;

Route::prefix('auth')->group(function () {
    Route::post('/register', [AuthController::class, 'register']);
    Route::post('/login', [AuthController::class, 'login']);
    Route::post('/forgot-password', [AuthController::class, 'forgotPassword']);

    Route::middleware('auth:sanctum')->group(function () {
        Route::post('/logout', [AuthController::class, 'logout']);
    });
});

Route::prefix('admin')->middleware(['auth:sanctum', 'admin'])->group(function () {
    Route::get('/dashboard', function () {
        return response()->json([
            'success' => true,
            'message' => 'Admin dashboard data',
            'data' => [
                'total_bookings' => 0,
                'active_bookings' => 0,
                'revenue' => 0,
                'fields_count' => 0,
            ],
        ]);
    });
});
