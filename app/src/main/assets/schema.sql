-- Reset schema for MySQL deployment
DROP DATABASE IF EXISTS `kanji_test`;
CREATE DATABASE `kanji_test`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE `kanji_test`;

-- Optional safety (keeps consistent behavior)
SET NAMES utf8mb4;
SET SQL_MODE = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS payment_transactions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS lesson_progress;
DROP TABLE IF EXISTS quiz_choices;
DROP TABLE IF EXISTS quiz_questions;
DROP TABLE IF EXISTS lesson_kanji_cross_ref;
DROP TABLE IF EXISTS course_unlocks;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS kanjis;
SET FOREIGN_KEY_CHECKS = 1;

-- Core tables
CREATE TABLE IF NOT EXISTS kanjis (
    kanji_id BIGINT PRIMARY KEY,
    characters VARCHAR(16) NOT NULL,
    meaning_vi VARCHAR(255) NOT NULL,
    meaning_en VARCHAR(255) NOT NULL,
    onyomi VARCHAR(128) NOT NULL,
    kunyomi VARCHAR(128) NOT NULL,
    stroke_count INT NOT NULL,
    jlpt_level VARCHAR(8) NOT NULL,
    example VARCHAR(128) NOT NULL,
    example_translation VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    level_tag VARCHAR(64) NOT NULL,
    level_order INT NOT NULL,
    cover_asset VARCHAR(128) NOT NULL,
    duration_minutes INT NOT NULL,
    is_premium TINYINT(1) NOT NULL DEFAULT 0,
    price_vnd INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lessons (
    lesson_id BIGINT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    order_index INT NOT NULL,
    duration_minutes INT NOT NULL,
    question_count INT NOT NULL,
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS course_unlocks (
    course_id BIGINT PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    payment_method VARCHAR(32),
    transaction_reference VARCHAR(64),
    unlocked_at BIGINT,
    CONSTRAINT fk_unlock_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lesson_kanji_cross_ref (
    lesson_id BIGINT NOT NULL,
    kanji_id BIGINT NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY (lesson_id, kanji_id),
    CONSTRAINT fk_lk_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    CONSTRAINT fk_lk_kanji FOREIGN KEY (kanji_id) REFERENCES kanjis(kanji_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quiz_questions (
    question_id BIGINT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    explanation TEXT NOT NULL,
    order_index INT NOT NULL,
    CONSTRAINT fk_question_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quiz_choices (
    choice_id BIGINT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    is_correct TINYINT(1) NOT NULL,
    CONSTRAINT fk_choice_question FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lesson_progress (
    lesson_id BIGINT PRIMARY KEY,
    best_score INT NOT NULL,
    last_score INT NOT NULL,
    completed TINYINT(1) NOT NULL,
    review_count INT NOT NULL,
    last_reviewed_at BIGINT,
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    reference VARCHAR(64) NOT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_payment_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    role_id BIGINT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    display_name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    avatar_url VARCHAR(255),
    created_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Kanji data
INSERT INTO kanjis(kanji_id, characters, meaning_vi, meaning_en, onyomi, kunyomi, stroke_count, jlpt_level, example, example_translation) VALUES
(101, '日', 'ngày; mặt trời', 'sun; day', 'ニチ, ジツ', 'ひ, -び, -か', 4, 'N5', '休日', 'ngày nghỉ / day off'),
(102, '月', 'trăng; tháng', 'moon; month', 'ゲツ, ガツ', 'つき', 4, 'N5', '月曜日', 'thứ hai / Monday'),
(103, '火', 'lửa', 'fire', 'カ', 'ひ, -び', 4, 'N5', '火山', 'núi lửa / volcano'),
(104, '水', 'nước', 'water', 'スイ', 'みず', 4, 'N5', '水道', 'nước máy / water supply'),
(105, '木', 'cây', 'tree; wood', 'モク, ボク', 'き, こ-', 4, 'N5', '木曜日', 'thứ năm / Thursday'),
(106, '金', 'vàng; tiền', 'gold; money', 'キン, コン', 'かね, かな-', 8, 'N5', 'お金', 'tiền / money'),
(107, '土', 'đất', 'earth; soil', 'ド, ト', 'つち', 3, 'N5', '土木', 'công trình / civil engineering'),
(108, '学', 'học', 'study', 'ガク', 'まな-ぶ', 8, 'N5', '大学', 'đại học / university'),
(109, '校', 'trường học', 'school', 'コウ', '', 10, 'N5', '学校', 'trường học / school'),
(110, '生', 'sinh; sống', 'life; birth', 'セイ, ショウ', 'い-きる, う-まれる', 5, 'N5', '学生', 'học sinh / student'),
(111, '時', 'thời gian', 'time; hour', 'ジ', 'とき', 10, 'N5', '時間', 'giờ giấc / time'),
(112, '見', 'nhìn', 'see; look', 'ケン', 'み-る', 7, 'N5', '見学', 'tham quan / study tour'),
(201, '話', 'nói chuyện', 'talk; story', 'ワ', 'はな-す, はなし', 13, 'N4', '会話', 'hội thoại / conversation'),
(202, '聞', 'nghe', 'listen', 'ブン, モン', 'き-く', 14, 'N4', '新聞', 'báo chí / newspaper'),
(203, '読', 'đọc', 'read', 'ドク', 'よ-む', 14, 'N4', '読書', 'đọc sách / reading'),
(204, '書', 'viết', 'write', 'ショ', 'か-く', 10, 'N4', '図書館', 'thư viện / library'),
(205, '体', 'cơ thể', 'body', 'タイ, テイ', 'からだ', 7, 'N4', '体育', 'thể dục / physical education'),
(206, '休', 'nghỉ', 'rest', 'キュウ', 'やす-む', 6, 'N4', '休日', 'ngày nghỉ / holiday'),
(301, '社', 'công ty', 'company', 'シャ', 'やしろ', 7, 'N3', '会社', 'công ty / company'),
(302, '員', 'nhân viên', 'member; employee', 'イン', '', 10, 'N3', '店員', 'nhân viên cửa hàng / clerk'),
(303, '会', 'hội; gặp', 'meeting', 'カイ, エ', 'あ-う', 6, 'N3', '会議', 'hội nghị / meeting'),
(304, '議', 'nghị luận', 'discussion', 'ギ', '', 20, 'N2', '議論', 'tranh luận / debate'),
(305, '業', 'nghiệp vụ', 'business', 'ギョウ, ゴウ', 'わざ', 13, 'N2', '営業', 'kinh doanh / sales'),
(306, '経', 'kinh tế', 'manage', 'ケイ, キョウ', 'へ-る', 11, 'N2', '経営', 'quản trị / management'),
(307, '管', 'quản lý', 'control', 'カン', 'くだ', 14, 'N2', '管理', 'quản lý / administration'),
(308, '理', 'lý', 'logic', 'リ', '', 11, 'N2', '料理', 'nấu ăn / cuisine');

-- Seed courses
INSERT INTO courses(course_id, title, slug, description, level_tag, level_order, cover_asset, duration_minutes, is_premium, price_vnd) VALUES
(1, 'Nền tảng JLPT N5', 'jlpt-n5-core', 'Làm quen 214 bộ thủ và từ vựng nền tảng qua hình ảnh và câu chuyện.', 'N5 Cơ bản', 1, 'ic_sakura_blossom', 45, 0, 0),
(2, 'Tăng tốc JLPT N4', 'jlpt-n4-speed', 'Các Kanji xuất hiện thường xuyên trong đề thi N4 kèm mẹo nhớ nhanh.', 'N4 Nâng cao', 2, 'ic_kanji_placeholder', 60, 1, 79000),
(3, 'Kanji giao tiếp doanh nghiệp', 'business-kanji', 'Mở rộng vốn kanji liên quan đến công việc văn phòng và họp hành.', 'Business', 3, 'ic_profile', 55, 1, 119000);

-- Seed lessons
INSERT INTO lessons(lesson_id, course_id, title, summary, order_index, duration_minutes, question_count) VALUES
(1001, 1, 'Ngày và chu kỳ thời gian', 'Học nhóm Kanji đại diện cho ngày, tháng và các yếu tố thiên nhiên.', 1, 15, 4),
(1002, 1, 'Trường lớp và nghề nghiệp', 'Mở rộng chủ đề trường học, sinh viên và nghề.', 2, 15, 4),
(1003, 1, 'Thời gian và hoạt động hàng ngày', 'Ghi nhớ cách biểu đạt thời gian và các động tác cơ bản.', 3, 15, 4),
(2001, 2, 'Kỹ năng ngôn ngữ', 'Tập trung vào các kanji liên quan tới nghe, nói, đọc, viết.', 1, 18, 4),
(2002, 2, 'Cơ thể và sức khỏe', 'Ôn tập nhóm kanji diễn tả cơ thể và trạng thái nghỉ ngơi.', 2, 18, 4),
(2003, 2, 'Thực hành tổng hợp', 'Áp dụng kanji vào các tình huống thực tế.', 3, 24, 4),
(3001, 3, 'Giao tiếp công sở', 'Từ vựng mô tả tổ chức và con người trong doanh nghiệp.', 1, 20, 3),
(3002, 3, 'Chiến lược và quản trị', 'Thuật ngữ liên quan tới quản lý và vận hành doanh nghiệp.', 2, 20, 3);

-- Default unlock states (course 1 free)
INSERT INTO course_unlocks(course_id, status, payment_method, transaction_reference, unlocked_at) VALUES
(1, 'FREE', NULL, NULL, UNIX_TIMESTAMP() * 1000);

-- Seed roles
INSERT INTO roles(role_id, code, display_name) VALUES
(1, 'LEARNER', 'Học viên'),
(2, 'MENTOR', 'Người hướng dẫn');

-- Seed sample users
INSERT INTO users(user_id, email, password_hash, display_name, avatar_url, created_at) VALUES
(1, 'learner@kanji.app', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'Lan Learner', NULL, UNIX_TIMESTAMP() * 1000),
(2, 'mentor@kanji.app', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'Minh Mentor', NULL, UNIX_TIMESTAMP() * 1000);

INSERT INTO user_roles(user_id, role_id) VALUES
(1, 1),
(2, 2);

-- Lesson <> Kanji mapping
INSERT INTO lesson_kanji_cross_ref(lesson_id, kanji_id, position) VALUES
(1001, 101, 1),
(1001, 102, 2),
(1001, 103, 3),
(1001, 104, 4),
(1002, 105, 1),
(1002, 106, 2),
(1002, 107, 3),
(1002, 108, 4),
(1002, 109, 5),
(1002, 110, 6),
(1003, 111, 1),
(1003, 112, 2),
(1003, 106, 3),
(1003, 206, 4),
(2001, 201, 1),
(2001, 202, 2),
(2001, 203, 3),
(2001, 204, 4),
(2002, 205, 1),
(2002, 206, 2),
(2002, 110, 3),
(2002, 111, 4),
(2003, 108, 1),
(2003, 201, 2),
(2003, 203, 3),
(2003, 206, 4),
(3001, 301, 1),
(3001, 302, 2),
(3001, 303, 3),
(3002, 304, 1),
(3002, 305, 2),
(3002, 306, 3),
(3002, 307, 4),
(3002, 308, 5);

-- Quiz questions
INSERT INTO quiz_questions(question_id, lesson_id, prompt, explanation, order_index) VALUES
(5001, 1001, 'Kanji nào mang nghĩa "mặt trời, ngày"?', 'Gợi ý: xuất hiện trong từ 日本.', 1),
(5002, 1001, 'Chọn kanji biểu thị "nước".', 'Từ khóa: 水道.', 2),
(5003, 1001, 'Kanji nào được dùng cho "lửa"?', 'Liên hệ với 火山.', 3),
(5004, 1001, 'Kanji nào dùng để chỉ "tháng"?', 'Xuất hiện trong 月曜日.', 4),
(5005, 1002, 'Kanji nào nghĩa là "trường học"?', 'Từ ghép phổ biến: 学校.', 1),
(5006, 1002, 'Chọn Kanji chỉ "học sinh".', 'Ghép bởi 学 và 生.', 2),
(5007, 1002, 'Kanji nào gắn với "tiền bạc"?', 'Xuất hiện trong お金.', 3),
(5008, 1002, 'Kanji nào nghĩa là "đất"?', 'Từ vựng 土木.', 4),
(5009, 1003, 'Kanji nào nghĩa là "thời gian"?', 'Nhìn thấy trong 時間.', 1),
(5010, 1003, 'Kanji nào đọc là みる và nghĩa là "nhìn"?', 'Gợi ý: 見学.', 2),
(5011, 1003, 'Kanji nào nghĩa là "nghỉ ngơi"?', 'Cặp với 日 tạo thành ngày nghỉ.', 3),
(5012, 1003, 'Kanji nào nghĩa là "tiền"?', 'Nhắc lại bài trước.', 4),
(5013, 2001, 'Kanji nào nghĩa là "nói chuyện"?', 'Từ gốc: 会話.', 1),
(5014, 2001, 'Chọn kanji nghĩa "nghe".', 'Gặp trong 新聞.', 2),
(5015, 2001, 'Kanji nào nghĩa là "đọc"?', 'Từ: 読書.', 3),
(5016, 2001, 'Kanji nào nghĩa là "viết"?', 'Từ: 図書館.', 4),
(5017, 2002, 'Kanji nào nghĩa là "cơ thể"?', 'Xuất hiện trong 体育.', 1),
(5018, 2002, 'Kanji nào nghĩa là "nghỉ ngơi"?', 'Kết hợp với 日.', 2),
(5019, 2002, 'Kanji nào nghĩa là "sinh"?', 'Gần gũi với từ 学生.', 3),
(5020, 2002, 'Kanji nào nghĩa là "thời gian"?', 'Ôn lại bài trước.', 4),
(5021, 2003, 'Kanji nào nghĩa là "học"?', 'Xuất hiện trong 学校.', 1),
(5022, 2003, 'Kanji nào nghĩa là "nói chuyện"?', 'Ôn tập 会話.', 2),
(5023, 2003, 'Kanji nào nghĩa là "đọc"?', 'Ôn tập 読書.', 3),
(5024, 2003, 'Kanji nào nghĩa là "nghỉ"?', 'Ôn tập 休日.', 4),
(5025, 3001, 'Kanji nào biểu thị "công ty"?', 'Từ khoá: 会社.', 1),
(5026, 3001, 'Chọn kanji nghĩa là "nhân viên".', 'Từ khoá: 店員.', 2),
(5027, 3001, 'Kanji nào nghĩa là "hội họp"?', 'Từ khoá: 会議.', 3),
(5028, 3002, 'Kanji nào nghĩa là "nghị luận"?', 'Từ khoá: 議論.', 1),
(5029, 3002, 'Kanji nào nghĩa là "kinh doanh"?', 'Từ khoá: 営業.', 2),
(5030, 3002, 'Kanji nào nghĩa là "quản lý"?', 'Từ khoá: 管理.', 3);

-- Quiz choices
INSERT INTO quiz_choices(choice_id, question_id, content, is_correct) VALUES
(6001, 5001, '日', 1),
(6002, 5001, '月', 0),
(6003, 5001, '火', 0),
(6004, 5001, '水', 0),
(6005, 5002, '金', 0),
(6006, 5002, '水', 1),
(6007, 5002, '土', 0),
(6008, 5002, '木', 0),
(6009, 5003, '火', 1),
(6010, 5003, '日', 0),
(6011, 5003, '水', 0),
(6012, 5003, '金', 0),
(6013, 5004, '月', 1),
(6014, 5004, '土', 0),
(6015, 5004, '学', 0),
(6016, 5004, '見', 0),
(6017, 5005, '校', 1),
(6018, 5005, '学', 0),
(6019, 5005, '生', 0),
(6020, 5005, '休', 0),
(6021, 5006, '学生', 0),
(6022, 5006, '生', 1),
(6023, 5006, '体', 0),
(6024, 5006, '金', 0),
(6025, 5007, '金', 1),
(6026, 5007, '土', 0),
(6027, 5007, '火', 0),
(6028, 5007, '月', 0),
(6029, 5008, '土', 1),
(6030, 5008, '水', 0),
(6031, 5008, '木', 0),
(6032, 5008, '見', 0),
(6033, 5009, '時', 1),
(6034, 5009, '見', 0),
(6035, 5009, '休', 0),
(6036, 5009, '学', 0),
(6037, 5010, '見', 1),
(6038, 5010, '読', 0),
(6039, 5010, '休', 0),
(6040, 5010, '金', 0),
(6041, 5011, '休', 1),
(6042, 5011, '金', 0),
(6043, 5011, '学', 0),
(6044, 5011, '見', 0),
(6045, 5012, '金', 1),
(6046, 5012, '土', 0),
(6047, 5012, '時', 0),
(6048, 5012, '見', 0),
(6049, 5013, '話', 1),
(6050, 5013, '聞', 0),
(6051, 5013, '読', 0),
(6052, 5013, '書', 0),
(6053, 5014, '聞', 1),
(6054, 5014, '話', 0),
(6055, 5014, '読', 0),
(6056, 5014, '書', 0),
(6057, 5015, '読', 1),
(6058, 5015, '書', 0),
(6059, 5015, '聞', 0),
(6060, 5015, '話', 0),
(6061, 5016, '書', 1),
(6062, 5016, '読', 0),
(6063, 5016, '聞', 0),
(6064, 5016, '話', 0),
(6065, 5017, '体', 1),
(6066, 5017, '休', 0),
(6067, 5017, '生', 0),
(6068, 5017, '時', 0),
(6069, 5018, '休', 1),
(6070, 5018, '金', 0),
(6071, 5018, '体', 0),
(6072, 5018, '生', 0),
(6073, 5019, '生', 1),
(6074, 5019, '見', 0),
(6075, 5019, '読', 0),
(6076, 5019, '書', 0),
(6077, 5020, '時', 1),
(6078, 5020, '見', 0),
(6079, 5020, '休', 0),
(6080, 5020, '金', 0),
(6081, 5021, '学', 1),
(6082, 5021, '書', 0),
(6083, 5021, '読', 0),
(6084, 5021, '休', 0),
(6085, 5022, '話', 1),
(6086, 5022, '聞', 0),
(6087, 5022, '読', 0),
(6088, 5022, '書', 0),
(6089, 5023, '読', 1),
(6090, 5023, '聞', 0),
(6091, 5023, '書', 0),
(6092, 5023, '体', 0),
(6093, 5024, '休', 1),
(6094, 5024, '金', 0),
(6095, 5024, '学', 0),
(6096, 5024, '読', 0),
(6097, 5025, '社', 1),
(6098, 5025, '員', 0),
(6099, 5025, '会', 0),
(6100, 5025, '業', 0),
(6101, 5026, '員', 1),
(6102, 5026, '社', 0),
(6103, 5026, '会', 0),
(6104, 5026, '議', 0),
(6105, 5027, '会', 1),
(6106, 5027, '議', 0),
(6107, 5027, '管', 0),
(6108, 5027, '理', 0),
(6109, 5028, '議', 1),
(6110, 5028, '会', 0),
(6111, 5028, '業', 0),
(6112, 5028, '管', 0),
(6113, 5029, '業', 1),
(6114, 5029, '理', 0),
(6115, 5029, '会', 0),
(6116, 5029, '員', 0),
(6117, 5030, '管', 1),
(6118, 5030, '理', 0),
(6119, 5030, '業', 0),
(6120, 5030, '議', 0);
