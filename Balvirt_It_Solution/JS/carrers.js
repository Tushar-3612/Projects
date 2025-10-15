document.addEventListener('DOMContentLoaded', function() {
            // Slider functionality
            const slider = document.querySelector('.slider');
            const cards = document.querySelectorAll('.card');
            const leftArrow = document.querySelector('.arrow.left');
            const rightArrow = document.querySelector('.arrow.right');
            const indicators = document.querySelectorAll('.indicator');
            
            let currentIndex = 0;
            const totalCards = cards.length;
            
            // Update slider position
            function updateSlider() {
                slider.style.transform = `translateX(-${currentIndex * 100}%)`;
                
                // Update indicators
                indicators.forEach((indicator, index) => {
                    indicator.classList.toggle('active', index === currentIndex);
                });
            }
            
            // Next slide
            function nextSlide() {
                currentIndex = (currentIndex + 1) % totalCards;
                updateSlider();
            }
            
            // Previous slide
            function prevSlide() {
                currentIndex = (currentIndex - 1 + totalCards) % totalCards;
                updateSlider();
            }
            
            // Arrow click events
            leftArrow.addEventListener('click', prevSlide);
            rightArrow.addEventListener('click', nextSlide);
            
            // Indicator click events
            indicators.forEach(indicator => {
                indicator.addEventListener('click', function() {
                    currentIndex = parseInt(this.getAttribute('data-index'));
                    updateSlider();
                });
            });
            
            // Auto slide every 5 seconds
            setInterval(nextSlide, 10000);
            
            // Modal functionality
            const modalOverlay = document.getElementById('modalOverlay');
            const closeBtn = document.getElementById('closeBtn');
            const jobTitleSpan = document.getElementById('jobTitle');
            const hiddenJobPosition = document.getElementById('hiddenJobPosition');
            const hireForm = document.getElementById('hireForm');
            const successMessage = document.getElementById('successMessage');
            
            // Apply Now button click events
            document.querySelectorAll('.apply-btn').forEach(button => {
                button.addEventListener('click', function() {
                    const job = this.getAttribute('data-job');
                    jobTitleSpan.textContent = job;
                    hiddenJobPosition.value = job;
                    modalOverlay.style.display = 'flex';
                    successMessage.style.display = 'none';
                    hireForm.style.display = 'block';
                });
            });
            
            // Close modal
            closeBtn.addEventListener('click', function() {
                modalOverlay.style.display = 'none';
            });
            
            // Close when clicking outside
            modalOverlay.addEventListener('click', function(e) {
                if (e.target === modalOverlay) {
                    modalOverlay.style.display = 'none';
                }
            });
            
            // Form submission
            hireForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                // Show success message
                hireForm.style.display = 'none';
                successMessage.style.display = 'block';
                
                // Reset form
                hireForm.reset();
                
                // Auto close modal after 3 seconds
                setTimeout(() => {
                    modalOverlay.style.display = 'none';
                }, 3000);
            });
        });

         // Show/Hide Hire Form Button based on scroll position
        document.addEventListener('DOMContentLoaded', function() {
            const freshersSection = document.getElementById('freshersSection');
            const hireFormButton = document.getElementById('hireFormButton');
            
            // Function to check if element is in viewport
            function isInViewport(element) {
                const rect = element.getBoundingClientRect();
                return (
                    rect.top <= (window.innerHeight || document.documentElement.clientHeight) &&
                    rect.bottom >= 0
                );
            }
            
            // Scroll event listener
            window.addEventListener('scroll', function() {
                if (isInViewport(freshersSection)) {
                    hireFormButton.classList.add('show');
                } else {
                    hireFormButton.classList.remove('show');
                }
            });
            
            // Initial check on page load
            if (isInViewport(freshersSection)) {
                hireFormButton.classList.add('show');
            }
        });