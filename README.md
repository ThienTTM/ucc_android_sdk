# ucc_android_sdk
Video call sample
Chạy Sample App bằng Android Studio:

1. Chọn menu File => Open Project. (Hoặc "Open an existing Project" nếu Android Studio đang mở)

2. Chọn đường dẫn đến thư mục chứa Sample App đã tải về.

3. Chọn Use default gradle wrapper (recommended) rồi nhấn OK.

4. Mở file app\cpp\native-lib.cpp (Java sample) / kotlin\cpp\native-lib.cpp (Kotlin sample)

5. Điền các giá trị của bộ key định danh vào các biến tương ứng.
```
std::string token_id = "";
std::string token_key = "";
std::string client_id = "";
std::string client_secret = "";
```
