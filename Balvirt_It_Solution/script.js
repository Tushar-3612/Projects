// Main JavaScript for Balvirt Website

// Image Slider Functionality
class ImageSlider {
    constructor() {
        this.slides = document.querySelectorAll('.slide');
        this.dotsContainer = document.querySelector('.slider-dots');
        this.prevBtn = document.querySelector('.prev');
        this.nextBtn = document.querySelector('.next');
        this.currentSlide = 0;
        this.autoSlideInterval = null;
        this.slider = document.querySelector('.hero-slider');
        this.isAnimating = false;
        
        // Swipe/touch variables
        this.touchStartX = 0;
        this.touchEndX = 0;
        this.swipeThreshold = 50;
        
        this.init();
    }
    
    init() {
        // Ensure first slide is active
        if (this.slides.length > 0) {
            this.slides[0].classList.add('active');
        }
        
        // Create dots
        if (this.dotsContainer && this.slides.length > 0) {
            this.slides.forEach((_, idx) => {
                const dot = document.createElement('div');
                dot.classList.add('dot');
                if (idx === 0) dot.classList.add('active');
                dot.addEventListener('click', () => this.goToSlide(idx));
                this.dotsContainer.appendChild(dot);
            });
        }
        
        // Event listeners
        if (this.prevBtn) {
            this.prevBtn.addEventListener('click', () => this.prevSlide());
        }
        
        if (this.nextBtn) {
            this.nextBtn.addEventListener('click', () => this.nextSlide());
        }
        
        // Start auto-sliding
        if (this.slides.length > 0) {
            this.startAutoSlide();
            
            // Pause on hover
            if (this.slider) {
                this.slider.addEventListener('mouseenter', () => this.stopAutoSlide());
                this.slider.addEventListener('mouseleave', () => this.startAutoSlide());
                
                // Add touch events for swipe functionality
                this.setupSwipeEvents();
            }
        }
    }
    
    setupSwipeEvents() {
        // Touch events for mobile swipe
        this.slider.addEventListener('touchstart', (e) => {
            this.touchStartX = e.changedTouches[0].screenX;
            this.stopAutoSlide();
        }, { passive: true });
        
        this.slider.addEventListener('touchend', (e) => {
            this.touchEndX = e.changedTouches[0].screenX;
            this.handleSwipe();
            this.startAutoSlide();
        }, { passive: true });
        
        // Mouse events for desktop drag/swipe
        this.slider.addEventListener('mousedown', (e) => {
            this.touchStartX = e.clientX;
            this.stopAutoSlide();
        });
        
        this.slider.addEventListener('mouseup', (e) => {
            this.touchEndX = e.clientX;
            this.handleSwipe();
            this.startAutoSlide();
        });
        
        // Prevent image drag
        this.slides.forEach(slide => {
            slide.addEventListener('dragstart', (e) => e.preventDefault());
        });
    }
    
    handleSwipe() {
        const diff = this.touchStartX - this.touchEndX;
        
        // Swipe left - next slide
        if (diff > this.swipeThreshold) {
            this.nextSlide();
        }
        // Swipe right - previous slide
        else if (diff < -this.swipeThreshold) {
            this.prevSlide();
        }
    }
    
    goToSlide(n) {
        if (this.slides.length === 0 || this.isAnimating) return;
        
        this.isAnimating = true;
        
        // Remove active class from current slide
        this.slides[this.currentSlide].classList.remove('active');
        
        // Remove active class from current dot
        const dots = document.querySelectorAll('.dot');
        if (dots.length > 0) {
            dots[this.currentSlide].classList.remove('active');
        }
        
        // Update current slide index
        this.currentSlide = (n + this.slides.length) % this.slides.length;
        
        // Add active class to new slide
        this.slides[this.currentSlide].classList.add('active');
        
        // Add active class to new dot
        if (dots.length > 0) {
            dots[this.currentSlide].classList.add('active');
        }
        
        // Reset animation flag after transition
        setTimeout(() => {
            this.isAnimating = false;
        }, 1000);
        
        // Reset auto-slide timer
        this.resetAutoSlide();
    }
    
    nextSlide() {
        this.goToSlide(this.currentSlide + 1);
    }
    
    prevSlide() {
        this.goToSlide(this.currentSlide - 1);
    }
    
    startAutoSlide() {
        // Clear existing interval
        this.stopAutoSlide();
        
        // Start new interval - change slide every 6 seconds
        this.autoSlideInterval = setInterval(() => {
            if (!this.isAnimating) {
                this.nextSlide();
            }
        }, 6000);
    }
    
    stopAutoSlide() {
        if (this.autoSlideInterval) {
            clearInterval(this.autoSlideInterval);
            this.autoSlideInterval = null;
        }
    }
    
    resetAutoSlide() {
        this.stopAutoSlide();
        this.startAutoSlide();
    }
}

// Navigation Management
class NavigationManager {
    constructor() {
        this.hamburger = document.querySelector(".hamburger");
        this.navMenu = document.querySelector(".nav-menu");
        this.dropdowns = document.querySelectorAll('.dropdown');
        this.scrollToTopBtn = document.querySelector('.scroll-to-top');
        this.init();
    }
    
    init() {
        this.setupMobileMenu();
        this.setupDropdowns();
        this.setupSmoothScrolling();
        this.setupScrollToTop();
        this.setupResizeHandler();
        this.setupClickOutsideHandler();
    }
    
    setupMobileMenu() {
        if (!this.hamburger || !this.navMenu) return;
        
        this.hamburger.addEventListener("click", () => {
            this.toggleMobileMenu();
        });
    }
    
    toggleMobileMenu() {
        this.hamburger.classList.toggle("active");
        this.navMenu.classList.toggle("active");
        
        // Close all dropdowns when menu is closed
        if (!this.navMenu.classList.contains('active')) {
            this.closeAllDropdowns();
        }
    }
    
    setupDropdowns() {
        // Mobile dropdown functionality
        document.querySelectorAll('.dropdown .nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                if (window.innerWidth <= 992) {
                    e.preventDefault();
                    const dropdown = e.currentTarget.parentElement;
                    this.toggleMobileDropdown(dropdown);
                }
            });
        });
        
        // Desktop dropdown functionality
        this.dropdowns.forEach(dropdown => {
            dropdown.addEventListener('mouseenter', () => {
                if (window.innerWidth > 992) {
                    dropdown.classList.add('active');
                }
            });
            
            dropdown.addEventListener('mouseleave', () => {
                if (window.innerWidth > 992) {
                    dropdown.classList.remove('active');
                }
            });
        });
    }
    
    toggleMobileDropdown(dropdown) {
        // Close other dropdowns
        this.dropdowns.forEach(d => {
            if (d !== dropdown) {
                d.classList.remove('active');
            }
        });
        
        dropdown.classList.toggle('active');
    }
    
    closeAllDropdowns() {
        this.dropdowns.forEach(d => {
            d.classList.remove('active');
        });
    }
    
    setupSmoothScrolling() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', (e) => {
                // Don't apply smooth scrolling for dropdown links on mobile
                if (window.innerWidth <= 992 && e.currentTarget.closest('.dropdown-content')) {
                    return;
                }
                
                e.preventDefault();
                const targetId = e.currentTarget.getAttribute('href');
                if (targetId === '#') return;
                
                this.scrollToTarget(targetId);
            });
        });
    }
    
    scrollToTarget(targetId) {
        const target = document.querySelector(targetId);
        if (target) {
            // Close mobile menu if open
            if (window.innerWidth <= 992) {
                this.closeMobileMenu();
            }
            
            const offsetTop = target.getBoundingClientRect().top + window.pageYOffset - 80;
            window.scrollTo({
                top: offsetTop,
                behavior: 'smooth'
            });
        }
    }
    
    setupScrollToTop() {
        if (!this.scrollToTopBtn) return;
        
        // Show/hide scroll to top button based on scroll position
        window.addEventListener('scroll', () => {
            if (window.pageYOffset > 300) {
                this.scrollToTopBtn.classList.add('show');
            } else {
                this.scrollToTopBtn.classList.remove('show');
            }
        });
        
        // Scroll to top when button is clicked
        this.scrollToTopBtn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
    
    closeMobileMenu() {
        if (this.hamburger) this.hamburger.classList.remove("active");
        if (this.navMenu) this.navMenu.classList.remove("active");
        this.closeAllDropdowns();
    }
    
    setupResizeHandler() {
        window.addEventListener('resize', () => {
            // Close mobile menu when switching to desktop
            if (window.innerWidth > 992) {
                this.closeMobileMenu();
            }
        });
    }
    
    setupClickOutsideHandler() {
        document.addEventListener('click', (e) => {
            if (window.innerWidth <= 992) {
                const isClickInsideNav = e.target.closest('.nav-menu');
                const isHamburger = e.target.closest('.hamburger');
                
                if (!isClickInsideNav && !isHamburger) {
                    this.closeMobileMenu();
                }
            } else {
                // Desktop - close dropdowns when clicking outside
                const isDropdown = e.target.closest('.dropdown');
                if (!isDropdown) {
                    this.closeAllDropdowns();
                }
            }
        });
    }
}

// Utility Functions
class Utilities {
    static preloadImages() {
        const imageUrls = [
            'images/home/first.png',
            'images/home/second.png',
            'images/home/netsuite.png',
            'images/home/home 2.jpg'
        ];

        imageUrls.forEach(url => {
            const img = new Image();
            img.src = url;
        });
    }

    static initializeTypingEffect() {
        if (typeof Typed === 'undefined') return null;

        // Har slide ke liye alag strings define
        const typingEffects = {
            "#typed-text": [
                "Bal<span style='color:#0074D9;'>virt</span>",
                "Where <span style='color:orange'>Innovation</span> Meets <span style='color:orange'>Execution</span>"
            ],
            "#typed-digital": ["Digital Transformation"],
            "#typed-netsuite": ["NetSuite <span style='color:orange' > ERP </span> Experts"],
            "#typed-consulting": ["IT <span style='color:orange' >Consulting </span> Excellence"]
        };

        Object.entries(typingEffects).forEach(([selector, strings]) => {
            if (document.querySelector(selector)) {
                new Typed(selector, {
                    strings: strings,
                    typeSpeed: 50,
                    backSpeed: 30,
                    backDelay: 2000,
                    loop: true,
                    showCursor: false,
                    contentType: 'html'
                });
            }
        });
    }

    static setInitialSliderHeight() {
        const slider = document.querySelector('.hero-slider');
        if (slider) {
            slider.style.height = window.innerHeight - 80 + 'px';
        }
    }
}

// Main Application Class
class BalvirtApp {
    constructor() {
        this.slider = null;
        this.navigation = null;
        this.typed = null;
        
        this.init();
    }
    
    init() {
        // Wait for DOM to be fully loaded
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                this.setupApplication();
            });
        } else {
            this.setupApplication();
        }
    }
    
    setupApplication() {
        // Initialize utilities
        Utilities.preloadImages();
        Utilities.setInitialSliderHeight();
        
        // Initialize core functionality
        this.slider = new ImageSlider();
        this.navigation = new NavigationManager();
        
        // Initialize typing effect
        this.typed = Utilities.initializeTypingEffect();
        
        // Set up global event listeners
        this.setupGlobalEvents();
    }
    
    setupGlobalEvents() {
        // Handle page load
        window.addEventListener('load', () => {
            Utilities.setInitialSliderHeight();
        });
        
        // Handle window resize for slider height
        window.addEventListener('resize', () => {
            Utilities.setInitialSliderHeight();
        });
    }
}

// Initialize the application
const balvirtApp = new BalvirtApp();

// Large screen navbar fix
function handleLargeScreenLayout() {
    const screenWidth = window.innerWidth;
    const navMenu = document.querySelector('.nav-menu');
    const hamburger = document.querySelector('.hamburger');
    
    if (screenWidth >= 993) {
        // Ensure desktop layout on large screens
        if (navMenu) {
            navMenu.classList.remove('active');
            navMenu.style.left = '';
        }
        if (hamburger) {
            hamburger.classList.remove('active');
        }
    }
}

// Run on load and resize
window.addEventListener('load', handleLargeScreenLayout);
window.addEventListener('resize', handleLargeScreenLayout);

// Close mobile menu when clicking nav links
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
        const screenWidth = window.innerWidth;
        if (screenWidth <= 992) {
            const navMenu = document.querySelector('.nav-menu');
            const hamburger = document.querySelector('.hamburger');
            if (navMenu) navMenu.classList.remove('active');
            if (hamburger) hamburger.classList.remove('active');
        }
    });
});