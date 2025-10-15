 
const form = document.querySelector('.newsletter-form');
const messageDiv = document.createElement('div');
messageDiv.style.marginTop = '10px';
messageDiv.style.fontWeight = 'bold';
messageDiv.style.color = 'green';
form.parentNode.insertBefore(messageDiv, form.nextSibling);

form.addEventListener('submit', function(e){
    e.preventDefault(); // Prevent page reload

    const formData = new FormData(form);

    fetch('php/newsletter.php', {
        method: 'POST',
        body: formData
    })
    .then(res => res.text())
    .then(data => {
        messageDiv.innerHTML = data; // Show success message
        form.reset(); // Clear input after submission
    })
    .catch(err => {
        messageDiv.innerHTML = "<span style='color:red;'>Error occurred. Try again.</span>";
    });
});
 