# E-Commerce Platform Backend

A comprehensive, production-ready RESTful API for a dual-portal e-commerce platform built with Spring Boot 4.0.1, MongoDB, Redis, and modern Java technologies.

## 🚀 Features

### Core Features
- ✅ **User Authentication & Authorization** - JWT-based authentication with refresh tokens
- ✅ **User Management** - Profile management, preferences, avatar upload
- ✅ **Address Management** - Multiple addresses with default selection
- ✅ **Shopping Cart** - Guest and authenticated user carts with merge functionality
- ✅ **Wishlist** - Save favorite products with price tracking
- ✅ **Product Catalog** - Advanced search, filters, and pagination
- ✅ **Order Management** - Complete checkout flow, tracking, and cancellation
- ✅ **Payment Processing** - Stripe integration (ready to implement)
- ✅ **Reviews & Ratings** - Product reviews with verified purchases
- ✅ **Vendor Portal** - Vendor registration, product management, analytics
- ✅ **Coupon System** - Discount coupons with validation
- ✅ **Email Notifications** - Transactional emails via SMTP/SendGrid

### Security Features
- 🔐 JWT access tokens (15 min expiry) and refresh tokens (7 days)
- 🔐 HTTP-only cookies for refresh tokens
- 🔐 Account lockout after failed login attempts
- 🔐 Password encryption with BCrypt
- 🔐 Email verification for new accounts
- 🔐 Rate limiting (ready to implement)
- 🔐 CORS configuration
- 🔐 Input validation and sanitization

### Technical Features
- 📊 MongoDB with proper indexing
- 🚀 Redis caching
- 📝 Comprehensive API documentation (Swagger/OpenAPI)
- 🔄 ModelMapper for DTO conversions
- 📧 Email service with templating
- ☁️ AWS S3 integration for file uploads (ready to implement)
- 💳 Stripe payment gateway (ready to implement)
- 🔍 Advanced product search and filtering
- 📱 Social OAuth (Google, GitHub) - ready to implement

## 📋 Prerequisites

- **Java 17 or higher** (configured for Java 25)
- **MongoDB 6.0+** - Running on `localhost:27017`
- **Redis 7.0+** - Running on `localhost:6379`
- **Maven 3.8+**
- **IDE** - IntelliJ IDEA, Eclipse, or VS Code

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.1 | Framework |
| Java | 25 | Programming Language |
| MongoDB | Latest | Database |
| Redis | Latest | Caching |
| JWT | 0.12.5 | Authentication |
| Swagger | 2.7.0 | API Documentation |
| Stripe | 27.7.0 | Payment Processing |
| AWS SDK | 2.29.32 | File Storage |
| ModelMapper | 3.2.1 | DTO Mapping |
| Lombok | Latest | Code Generation |

## 📁 Project Structure

```
ecom/
├── src/main/java/com/tcon/ecom/
│   ├── config/              # Configuration classes
│   │   ├── AppConfig.java
│   │   ├── SecurityConfig.java
│   │   └── OpenApiConfig.java
│   ├── controller/          # REST Controllers
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── AddressController.java
│   │   ├── CartController.java
│   │   ├── WishlistController.java
│   │   ├── ProductController.java
│   │   ├── OrderController.java
│   │   ├── VendorController.java
│   │   └── CouponController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/         # Request DTOs
│   │   └── response/        # Response DTOs
│   ├── model/               # Domain Models/Entities
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── Cart.java
│   │   ├── Wishlist.java
│   │   ├── Address.java
│   │   ├── PaymentMethod.java
│   │   ├── ProductReview.java
│   │   ├── OrderTracking.java
│   │   ├── Coupon.java
│   │   ├── enums/           # Enumerations
│   │   └── embedded/        # Embedded Documents
│   ├── repository/          # MongoDB Repositories
│   ├── service/             # Service Interfaces
│   ├── serviceImpl/         # Service Implementations
│   ├── security/            # Security Components
│   │   ├── JwtTokenProvider.java
│   │   └── CustomUserDetailsService.java
│   └── exception/           # Custom Exceptions
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       └── BadRequestException.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
└── pom.xml
```

## 🚦 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ecom-be/ecom
```

### 2. Configure Environment

Create a `.env` file or update `application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/ecom_db

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT Secret (Change in production!)
jwt.secret=your-very-long-and-secure-secret-key-here-at-least-256-bits

# Email (Gmail example)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# AWS S3 (Optional)
aws.s3.access-key=your-access-key
aws.s3.secret-key=your-secret-key
aws.s3.bucket-name=ecom-uploads

# Stripe (Optional)
stripe.api-key=sk_test_your-stripe-secret-key
```

### 3. Start MongoDB and Redis

```bash
# Start MongoDB
mongod --dbpath /path/to/data/db

# Start Redis
redis-server
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or with a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

## 🔑 API Endpoints Overview

### Authentication & Authorization
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/social/google` - Google OAuth login
- `POST /api/auth/social/github` - GitHub OAuth login

### User Management
- `GET /api/users/me` - Get current user
- `PATCH /api/users/me` - Update profile
- `PATCH /api/users/me/password` - Update password
- `PATCH /api/users/me/preferences` - Update preferences
- `POST /api/users/me/avatar` - Upload avatar

### Address Management
- `GET /api/users/me/addresses` - Get all addresses
- `POST /api/users/me/addresses` - Add address
- `PATCH /api/users/me/addresses/:id` - Update address
- `DELETE /api/users/me/addresses/:id` - Delete address
- `PATCH /api/users/me/addresses/:id/default` - Set default address

### Shopping Cart
- `GET /api/cart` - Get cart
- `POST /api/cart/items` - Add to cart
- `PATCH /api/cart/items/:id` - Update cart item
- `DELETE /api/cart/items/:id` - Remove from cart
- `DELETE /api/cart` - Clear cart
- `POST /api/cart/merge` - Merge guest cart

### Wishlist
- `GET /api/users/me/wishlist` - Get wishlist
- `POST /api/users/me/wishlist` - Add to wishlist
- `DELETE /api/users/me/wishlist/:productId` - Remove from wishlist
- `GET /api/users/me/wishlist/check/:productId` - Check if in wishlist

### Products
- `GET /api/products` - Get products (with filters)
- `GET /api/products/:slug` - Get product details
- `GET /api/products/featured` - Get featured products
- `GET /api/products/new-arrivals` - Get new arrivals
- `GET /api/products/:id/reviews` - Get product reviews

### Orders
- `POST /api/orders` - Create order (checkout)
- `GET /api/users/me/orders` - Get user orders
- `GET /api/users/me/orders/:orderNumber` - Get order details
- `GET /api/users/me/orders/:orderNumber/tracking` - Track order
- `POST /api/users/me/orders/:orderNumber/cancel` - Cancel order
- `POST /api/orders/:orderId/products/:productId/review` - Submit review

### Vendor Portal
- `POST /api/vendors/register` - Vendor registration
- `GET /api/vendors/dashboard/stats` - Dashboard statistics
- `GET /api/vendors/analytics` - Analytics data
- `GET /api/vendors/products` - Get vendor products
- `POST /api/vendors/products` - Create product
- `PATCH /api/vendors/products/:id` - Update product
- `DELETE /api/vendors/products/:id` - Delete product
- `GET /api/vendors/orders` - Get vendor orders
- `PATCH /api/vendors/orders/:orderNumber/status` - Update order status
- `POST /api/vendors/coupons` - Create coupon
- `GET /api/vendors/coupons` - Get coupons

### Coupons
- `POST /api/coupons/validate` - Validate coupon

## 🧪 Testing

Run all tests:

```bash
mvn test
```

Run with coverage:

```bash
mvn test jacoco:report
```

## 📦 Building for Production

```bash
# Build JAR
mvn clean package -DskipTests

# The JAR will be in target/ecom-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Support (To be added)

```dockerfile
# Dockerfile example
FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/ecom-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🔧 Configuration

Key configuration files:
- `application.properties` - Main configuration
- `application-dev.properties` - Development environment
- `application-prod.properties` - Production environment

## 🛡️ Security Best Practices

1. ✅ Change JWT secret in production
2. ✅ Use environment variables for sensitive data
3. ✅ Enable HTTPS in production
4. ✅ Configure CORS properly
5. ✅ Implement rate limiting
6. ✅ Regular security audits
7. ✅ Keep dependencies updated

## 📝 TODO / Future Enhancements

- [ ] Complete Stripe payment integration
- [ ] Implement AWS S3 file upload service
- [ ] Add Google OAuth2 implementation
- [ ] Add GitHub OAuth2 implementation
- [ ] Implement background job queues (BullMQ/Redis)
- [ ] Add email templates with HTML
- [ ] Implement real-time notifications (WebSocket)
- [ ] Add comprehensive unit and integration tests
- [ ] Set up CI/CD pipeline
- [ ] Add monitoring and logging (ELK Stack)
- [ ] Implement API rate limiting
- [ ] Add GraphQL support
- [ ] Multi-language support (i18n)
- [ ] Advanced analytics and reporting
- [ ] Mobile app API optimization

## 📄 License

This project is proprietary and confidential.

## 👥 Contributors

- Development Team @ Tcon

## 📞 Support

For support and queries, contact: support@tcon.com

---

**Built with ❤️ using Spring Boot and MongoDB**

