// Default Routine
const defaultRoutine = [
    { time: "07:00", task: "Wake up and freshen up" },
    { time: "07:30", task: "Healthy breakfast" },
    { time: "08:00", task: "Outdoor play or exercise" },
    { time: "12:00", task: "Lunch" },
    { time: "13:00", task: "Quiet time or reading" },
    { time: "14:00", task: "Creative activities" },
    { time: "17:30", task: "Dinner" },
    { time: "18:30", task: "Family time or games" },
    { time: "20:00", task: "Bedtime story" },
    { time: "20:30", task: "Sleep" }
];

// Request notification permission
function requestNotificationPermission() {
    if (Notification.permission !== "granted") {
        Notification.requestPermission();
    }
}

// Send a browser notification
function sendNotification(message) {
    if (Notification.permission === "granted") {
        const notification = new Notification(message, {
            body: "Click to view the website",
            icon: "logo.jpeg",
            requireInteraction: true
        });

        notification.onclick = () => {
            window.focus();
        };
    }
}

// Schedule notifications based on routine
function scheduleNotifications() {
    const routine = JSON.parse(localStorage.getItem("userRoutine")) || defaultRoutine;
    const now = new Date();

    routine.forEach(item => {
        const [hour, minute] = item.time.split(":").map(Number);
        const taskTime = new Date();
        taskTime.setHours(hour, minute, 0, 0);

        if (taskTime > now) {
            const timeDifference = taskTime - now; // time in milliseconds
            setTimeout(() => sendNotification(`Time to: ${item.task}`), timeDifference);
        }
    });
}

// Save routine to localStorage and update view
function saveRoutine(routine) {
    localStorage.setItem("userRoutine", JSON.stringify(routine));
    renderRoutine(routine);
    scheduleNotifications();
}

// Render the routine list in the UI
function renderRoutine(routine) {
    const routineList = document.getElementById("custom-routine-list");
    routineList.innerHTML = "";
    routine.forEach(item => {
        const listItem = document.createElement("li");
        listItem.textContent = `${item.time} - ${item.task}`;
        routineList.appendChild(listItem);
    });
}

// Accept the default routine
function acceptRoutine() {
    saveRoutine(defaultRoutine);
    alert("Default routine accepted!");
}

// Reset the routine
function resetRoutine() {
    localStorage.removeItem("userRoutine");
    document.getElementById("custom-routine-list").innerHTML = ""; // Clear the current list view
    alert("Routine has been reset! No routine is currently set.");
}

// Add a custom task to the routine
document.getElementById("custom-routine-form").addEventListener("submit", function (event) {
    event.preventDefault();
    const time = document.getElementById("time").value;
    const task = document.getElementById("task").value;

    if (!time || !task) {
        alert("Please fill out both the time and task.");
        return;
    }

    const customTask = { time: time, task: task };
    let routine = JSON.parse(localStorage.getItem("userRoutine")) || [];
    routine.push(customTask);
    saveRoutine(routine);
    alert("Task added successfully!");
});

// On page load, request notification permission and render saved routine
document.addEventListener("DOMContentLoaded", function () {
    requestNotificationPermission();

    // Load saved routine if it exists
    const savedRoutine = JSON.parse(localStorage.getItem("userRoutine"));
    if (savedRoutine) {
        renderRoutine(savedRoutine);
    }

    // Schedule checking notifications every minute
    setInterval(scheduleNotifications, 60000);
});
