<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    public function run(): void
    {
        User::factory()->create([
            'name' => 'Test User',
            'email' => 'test@example.com',
        ]);

        User::create([
            'name' => 'Admin FindFutsall',
            'username' => 'admin',
            'email' => 'admin@findfutsall.com',
            'password' => Hash::make('admin123'),
            'phone' => '081234567890',
            'role' => 'admin',
        ]);
    }
}
