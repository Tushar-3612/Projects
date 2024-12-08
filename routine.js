// Request permission for notifications
if (Notification.permission !== "granted") {
    Notification.requestPermission();
}

// Function to show a notification
function showNotification(title, body) {
    if (Notification.permission === "granted") {
        new Notification(title, {
            body: body,
            icon: "logo.jpeg", // You can change this to the logo of your website or app
        });
    }
}

// Function to accept the default routine
function acceptRoutine() {
    // Notify the user that they accepted the default routine
    showNotification("Routine Accepted", "You have accepted the default daily routine.");

    // Add tasks to custom routine (as default tasks)
    const defaultTasks = [
        { time: "7:00 AM", task: "Wake up and freshen up" },
        { time: "7:30 AM", task: "Healthy breakfast" },
        { time: "8:00 AM", task: "Outdoor play or exercise" },
        { time: "12:00 PM", task: "Lunch" },
        { time: "1:00 PM", task: "Quiet time or reading" },
        { time: "2:00 PM", task: "Creative activities" },
        { time: "5:30 PM", task: "Dinner" },
        { time: "6:30 PM", task: "Family time or games" },
        { time: "8:00 PM", task: "Bedtime story" },
        { time: "8:30 PM", task: "Sleep" }
    ];

    // Add default tasks to the custom routine list
    const customRoutineList = document.getElementById('custom-routine-list');
    customRoutineList.innerHTML = '';
    defaultTasks.forEach(task => {
        const listItem = document.createElement('li');
        listItem.textContent = `${task.time} - ${task.task}`;
        customRoutineList.appendChild(listItem);
    });
}

// Function to reset the routine
function resetRoutine() {
    // Notify the user that the routine has been reset
    showNotification("Routine Reset", "Your routine has been reset to default.");
    
    // Clear custom routine
    const customRoutineList = document.getElementById('custom-routine-list');
    customRoutineList.innerHTML = '';
}

// Function to handle form submission for custom routine
document.getElementById('custom-routine-form').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the form from refreshing the page
    
    // Get the task time and description from the form
    const taskTime = document.getElementById('time').value;
    const taskDescription = document.getElementById('task').value;

    // Create a new list item for the custom task
    const listItem = document.createElement('li');
    listItem.textContent = `${taskTime} - ${taskDescription}`;

    // Append the custom task to the custom routine list
    const customRoutineList = document.getElementById('custom-routine-list');
    customRoutineList.appendChild(listItem);

    // Show a notification for the new task
    showNotification("New Task Added", `${taskDescription} at ${taskTime}`);
});
