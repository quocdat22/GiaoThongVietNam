# Ứng dụng Học Biển Báo Giao Thông Việt Nam

## 1. Tổng Quan

Đây là một ứng dụng Android được thiết kế để giúp người dùng học và ghi nhớ các biển báo giao thông tại Việt Nam. Ứng dụng cung cấp các tính năng chính như xem danh sách biển báo, tìm kiếm, học qua thẻ ghi nhớ (flashcards), và làm bài kiểm tra (quiz).

## 2. Tính Năng Chính

*   **Danh sách Biển Báo:**
    *   Hiển thị danh sách đầy đủ các loại biển báo giao thông.
    *   Dữ liệu được lấy từ API và có cơ chế lưu trữ offline để sử dụng khi không có mạng.
    *   Phân loại biển báo (Cấm, Nguy hiểm, Chỉ dẫn...).
*   **Tìm Kiếm:**
    *   Tìm kiếm biển báo theo tên hoặc mã.
    *   Gợi ý tìm kiếm và lịch sử tìm kiếm.
    *   Lọc kết quả theo thời gian thực khi người dùng gõ.
*   **Thẻ Ghi Nhớ (Flashcards):**
    *   Xem các bộ thẻ ghi nhớ được tạo sẵn (Tất cả biển báo, theo loại).
    *   Tạo bộ thẻ ghi nhớ ngẫu nhiên.
    *   Tạo bộ thẻ tùy chỉnh theo loại biển báo.
    *   Học thẻ với giao diện lật thẻ.
    *   Lưu trữ các bộ thẻ đã tạo để sử dụng lại (`FlashcardDeckManager` sử dụng JSON).
    *   Chỉnh sửa (thêm/xóa/sửa thẻ) và xóa bộ thẻ.
    *   Tạo bản sao bộ thẻ.
*   **Kiểm Tra (Quiz):**
    *   Bắt đầu bài kiểm tra ngẫu nhiên từ tất cả các biển báo.
    *   (Đã lên kế hoạch) Bắt đầu bài kiểm tra theo danh mục biển báo.
    *   Giao diện làm bài kiểm tra với câu hỏi trắc nghiệm và hình ảnh.
    *   Xem lại kết quả và đáp án sau khi hoàn thành.
    *   Lưu trữ lịch sử các bài kiểm tra đã làm (`QuizHistoryManager` sử dụng SharedPreferences).
*   **Cài Đặt:**
    *   Chế độ tối (Dark Mode).
    *   Quản lý dữ liệu offline (Bật/tắt, xem thời gian tải cuối, xóa dữ liệu offline).

## 3. Kiến Trúc & Thành Phần Chính

*   **Ngôn ngữ:** Java
*   **Kiến trúc:** (Có thể chưa hoàn toàn theo MVVM) Sử dụng các Fragment cho từng màn hình chính, Activity cho các luồng chức năng phụ (Quiz, Study, Editor).
*   **Thư viện chính:**
    *   `AndroidX` (AppCompat, RecyclerView, ViewPager2, ConstraintLayout, Preference, Lifecycle)
    *   `Material Components` (Buttons, Cards, Dialogs, FAB, Toolbar, Slider)
    *   `Glide` (Tải và cache hình ảnh)
    *   `Retrofit` & `Gson` (Giao tiếp API và xử lý JSON)
*   **Thành phần quản lý dữ liệu:**
    *   `TrafficSignRepository`: Trung tâm quản lý dữ liệu biển báo, xử lý việc lấy dữ liệu từ API và cache offline.
    *   `OfflineDataManager`: Quản lý việc lưu/tải dữ liệu biển báo dưới dạng JSON cho chế độ offline.
    *   `DataManager`: Quản lý tập dữ liệu biển báo hiện tại trong bộ nhớ và thông báo thay đổi cho các listener (Fragment).
    *   `FlashcardDeckManager`: Quản lý việc lưu/tải các bộ thẻ ghi nhớ (FlashcardDeck) dưới dạng JSON.
    *   `QuizHistoryManager`: Quản lý lịch sử các bài kiểm tra đã làm, lưu trong SharedPreferences.
    *   `PrefsManager`: Quản lý các cài đặt đơn giản (như dark mode) trong SharedPreferences.

## 4. Lưu Trữ Dữ Liệu

*   **Dữ liệu Biển Báo (Offline):** Lưu trữ dưới dạng file JSON (`traffic_signs.json`) thông qua `OfflineDataManager`.
*   **Bộ Thẻ Ghi Nhớ:** Lưu trữ dưới dạng file JSON (`flashcard_decks.json`) thông qua `FlashcardDeckManager`.
*   **Lịch Sử Quiz:** Lưu trữ danh sách các đối tượng `Quiz` đã hoàn thành vào `SharedPreferences` dưới dạng JSON, quản lý bởi `QuizHistoryManager`.
*   **Cài Đặt:** Lưu các cài đặt như Dark Mode, trạng thái Offline Mode vào `SharedPreferences`.
*   **Cache Hình Ảnh:** `Glide` tự động quản lý việc cache hình ảnh trên bộ nhớ đệm (memory) và đĩa (disk).

## 5. Thiết Lập & Xây Dựng

1.  Clone repository dự án.
2.  Mở dự án bằng Android Studio.
3.  Đảm bảo có kết nối Internet để Gradle tải các thư viện cần thiết.
4.  Chạy lệnh `./gradlew assembleDebug` (hoặc `gradlew assembleDebug` trên Windows) để xây dựng file APK debug.
5.  Hoặc chạy trực tiếp trên máy ảo/thiết bị thật từ Android Studio.

## 6. Tích Hợp API

*   Ứng dụng sử dụng một API (chưa rõ endpoint cụ thể trong docs này) để lấy danh sách các biển báo giao thông.
*   `TrafficSignRepository` và `ApiService` (sử dụng Retrofit) chịu trách nhiệm giao tiếp với API.

## 7. Cải Thiện & Hướng Phát Triển

*   **Nâng cấp lên Room Database:** Thay thế việc lưu trữ JSON bằng Room để quản lý dữ liệu biển báo và bộ thẻ hiệu quả hơn, đặc biệt khi dữ liệu lớn.
*   **Triển khai đầy đủ MVVM:** Tách biệt rõ ràng hơn giữa UI, Logic và Data.
*   **Hoàn thiện tính năng Quiz:** Thêm chức năng tạo quiz theo danh mục.
*   **Thêm tính năng theo dõi tiến độ học tập:** Ghi nhận chi tiết hơn quá trình học của người dùng.
*   **Tối ưu hóa hiệu năng:** Kiểm tra và cải thiện hiệu năng tải dữ liệu, hiển thị hình ảnh.
*   **Viết Unit Test và UI Test.** 