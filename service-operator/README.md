# Service Operator

خدمة Spring Boot مستقلة تستخدم مكتبة `access-sdk` لتهيئة الأدوار والصلاحيات تلقائياً في Keycloak.

## متطلبات التشغيل
- Java 17+
- خادم Keycloak مشغل
- تم تثبيت `access-sdk` محلياً عبر أمر:
  ```bash
  mvn clean install
  ```
  داخل مجلد `access-sdk`.

## طريقة التشغيل
1. فك الضغط وانتقل إلى مجلد المشروع:
   ```bash
   cd service-operator
   ```
2. قم بتشغيل التطبيق عبر Maven:
   ```bash
   mvn spring-boot:run
   ```
   أو قم بالبناء أولاً:
   ```bash
   mvn clean package -DskipTests
   java -jar target/service-operator-1.0.0.jar
   ```

الخدمة تعمل افتراضياً على المنفذ: `8082` لتجنب التعارض مع المنافذ الأخرى.
