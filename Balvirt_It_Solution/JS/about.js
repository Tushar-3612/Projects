 
        // DOM Content Loaded
        document.addEventListener('DOMContentLoaded', function () {
            // Typing Animation
            const titleElement = document.getElementById('typed-title');
            const text = "";
            let index = 0;
            
            function typeWriter() {
                if (index < text.length) {
                    titleElement.innerHTML += text.charAt(index);
                    index++;
                    setTimeout(typeWriter, 100);
                }
            }
            
            typeWriter();
            
            // Scroll Animation for Sections
            const observerOptions = {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            };
            
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('visible');
                        
                        // Animate list items with delay
                        if (entry.target.querySelector('.list-item')) {
                            const listItems = entry.target.querySelectorAll('.list-item');
                            listItems.forEach((item, index) => {
                                setTimeout(() => {
                                    item.classList.add('visible');
                                }, index * 200);
                            });
                        }
                        
                        // Animate stats circles
                        if (entry.target.querySelector('.stats-circle')) {
                            const statsCircles = entry.target.querySelectorAll('.stats-circle');
                            statsCircles.forEach((circle, index) => {
                                setTimeout(() => {
                                    circle.classList.add('visible');
                                    
                                    // Animate the counter
                                    const target = +circle.getAttribute('data-target');
                                    const numberElement = circle.querySelector('.stat-number');
                                    let count = 0;
                                    const increment = target / 100;
                                    
                                    const updateCount = () => {
                                        if (count < target) {
                                            count += increment;
                                            numberElement.innerText = Math.ceil(count);
                                            setTimeout(updateCount, 20);
                                        } else {
                                            numberElement.innerText = target;
                                            circle.classList.add('animated');
                                        }
                                    };
                                    
                                    updateCount();
                                }, index * 300);
                            });
                        }
                        
                        // Animate values list
                        if (entry.target.querySelector('.value-item')) {
                            const valueItems = entry.target.querySelectorAll('.value-item');
                            valueItems.forEach((item, index) => {
                                setTimeout(() => {
                                    item.classList.add('visible');
                                }, index * 150);
                            });
                        }
                    }
                });
            }, observerOptions);
            
            // Observe all sections
            document.querySelectorAll('.about-row, .values-section').forEach(section => {
                observer.observe(section);
            });
            
            // Image Slider
            const sliderTrack = document.querySelector('.bv-slider-track');
            const slides = document.querySelectorAll('.bv-slide-item');
            const prevBtn = document.querySelector('.bv-prev-btn');
            const nextBtn = document.querySelector('.bv-next-btn');
            const dotsContainer = document.querySelector('.bv-slider-dots');
            
            let currentSlide = 0;
            const slideCount = slides.length;
            
            // Create dots
            slides.forEach((_, index) => {
                const dot = document.createElement('div');
                dot.classList.add('bv-dot');
                if (index === 0) dot.classList.add('active');
                dot.addEventListener('click', () => goToSlide(index));
                dotsContainer.appendChild(dot);
            });
            
            const dots = document.querySelectorAll('.bv-dot');
            
            // Function to go to a specific slide
            function goToSlide(slideIndex) {
                slides[currentSlide].classList.remove('active');
                dots[currentSlide].classList.remove('active');
                
                currentSlide = slideIndex;
                
                slides[currentSlide].classList.add('active');
                dots[currentSlide].classList.add('active');
                sliderTrack.style.transform = `translateX(-${currentSlide * 100}%)`;
            }
            
            // Next slide function
            function nextSlide() {
                const nextIndex = (currentSlide + 1) % slideCount;
                goToSlide(nextIndex);
            }
            
            // Previous slide function
            function prevSlide() {
                const prevIndex = (currentSlide - 1 + slideCount) % slideCount;
                goToSlide(prevIndex);
            }
            
            // Event listeners for buttons
            nextBtn.addEventListener('click', nextSlide);
            prevBtn.addEventListener('click', prevSlide);
            
            // Auto slide every 5 seconds
            let slideInterval = setInterval(nextSlide, 5000);
            
            // Pause auto slide on hover
            const sliderContainer = document.querySelector('.bv-slider-container');
            sliderContainer.addEventListener('mouseenter', () => {
                clearInterval(slideInterval);
            });
            
            sliderContainer.addEventListener('mouseleave', () => {
                slideInterval = setInterval(nextSlide, 5000);
            });
            
            // Keyboard navigation
            document.addEventListener('keydown', (e) => {
                if (e.key === 'ArrowLeft') {
                    prevSlide();
                } else if (e.key === 'ArrowRight') {
                    nextSlide();
                }
            });
            
            // Touch swipe support for mobile
            let startX = 0;
            let endX = 0;
            
            sliderContainer.addEventListener('touchstart', (e) => {
                startX = e.touches[0].clientX;
            });
            
            sliderContainer.addEventListener('touchend', (e) => {
                endX = e.changedTouches[0].clientX;
                handleSwipe();
            });
            
            function handleSwipe() {
                if (startX - endX > 50) {
                    // Swipe left
                    nextSlide();
                } else if (endX - startX > 50) {
                    // Swipe right
                    prevSlide();
                }
            }
            
            // Interactive cards click effect
            document.querySelectorAll('.interactive-card').forEach(card => {
                card.addEventListener('click', function() {
                    this.style.transform = 'translateY(-10px) scale(1.02)';
                    setTimeout(() => {
                        this.style.transform = 'translateY(-10px)';
                    }, 150);
                });
            });

            // Scroll to Top Functionality
            const scrollToTopBtn = document.querySelector('.scroll-to-top');
            
            window.addEventListener('scroll', () => {
                if (window.pageYOffset > 300) {
                    scrollToTopBtn.classList.add('visible');
                } else {
                    scrollToTopBtn.classList.remove('visible');
                }
            });
            
            scrollToTopBtn.addEventListener('click', () => {
                window.scrollTo({
                    top: 0,
                    behavior: 'smooth'
                });
            });
        });