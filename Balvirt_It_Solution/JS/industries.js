 
        // Industries Page Specific JavaScript
        document.addEventListener('DOMContentLoaded', function() {
            // Animate stats counter
            const statNumbers = document.querySelectorAll('.industries-stat-number');
            
            function animateStats() {
                statNumbers.forEach(stat => {
                    const target = parseInt(stat.getAttribute('data-target'));
                    const duration = 2000;
                    const step = target / (duration / 16);
                    let current = 0;
                    
                    const timer = setInterval(() => {
                        current += step;
                        if (current >= target) {
                            stat.textContent = target + '+';
                            clearInterval(timer);
                        } else {
                            stat.textContent = Math.floor(current) + '+';
                        }
                    }, 16);
                });
            }
            
            // Intersection Observer for stats animation
            const statsSection = document.querySelector('.industries-stats-section');
            const statsObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        animateStats();
                        statsObserver.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.5 });
            
            if (statsSection) {
                statsObserver.observe(statsSection);
            }
            
            // Intersection Observer for industry items
            const industryItems = document.querySelectorAll('.industries-item');
            const industryObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('industries-visible');
                        industryObserver.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.2 });
            
            industryItems.forEach(item => {
                industryObserver.observe(item);
            });
            
            // Contact form handling
            const contactForm = document.getElementById('industriesContactForm');
            if (contactForm) {
                contactForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    // Here you would typically send the form data to your server
                    alert('Thank you for your message! We will get back to you soon.');
                    contactForm.reset();
                });
            }
        });
     