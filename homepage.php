
<?php
session_start();
include("connect.php");

?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Health Tracker</title>
    <link rel="stylesheet" href="index.css">
    <link rel="stylesheet" href="services.css">
    <style>
     .hero-content {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: white;
        }
        .special-button {
            background-color: #0072ff; 
            color: white;
            border: none;
            border-radius: 5px;
            padding: 10px 15px;
            cursor: pointer;
            font-size: 1em;
            transition: background 0.3s;
            text-decoration: none; /* Remove underline */
            display: inline-block; /* Allow margin and padding */
            margin-top: 10px; /* Space above the button */
            margin-right: 10px;
        }
        .special-button:hover {
            background-color: #0072ff; 
        }.hero-content {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: white;
        }
        .special-button {
            background-color: #0072ff; 
            color: #f3f8ff;
            border: none;
            border-radius: 5px;
            padding: 10px 15px;
            cursor: pointer;
            font-size: 1em;
            transition: background 0.3s;
            text-decoration: none; /* Remove underline */
            display: inline-block; /* Allow margin and padding */
            margin-top: 10px; /* Space above the button */
            margin-right: 10px;
        }
        .special-button:hover {
            background-color: #0072ff; 
        }
        /* Center the content of the blog section */
        #blog {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            padding: 20px;
        }

        /* Style for each article */
        #blog article {
            width: 80%; /* Adjust this to control the width of each article */
            margin-bottom: 20px;
            background-color: #fbfdff; /* Light background for each article */
            padding: 20px;
            border-radius: 8px; /* Optional: gives a rounded corner effect */
        }

        /* Make the images bigger and center them */
        .blog-img {
            width: 100%; /* Make images take up the full width of their container */
            height: auto; /* Maintain the aspect ratio */
            max-width: 600px; /* Optional: Max width for better control */
            margin: 0 auto 15px auto; /* Centers the image and adds space below */
        }

        /* Adjust the header and links styles */
        #blog h3, #blog h4 {
            font-size: 1.5em;
            color: #0072ff; /* Make the heading text blue */
            margin-bottom: 10px;
        }

        /* Style the anchor tags inside articles */
        #blog a {
            color: #0072ff; /* Blue color for links */
            text-decoration: none;
        }

        #blog a:hover {
            text-decoration: underline; /* Underline on hover for better interaction */
        }
    </style>
</head>

<body>
    <header>
        
        <div class="logo">
            <img src="logo.jpeg" alt="User Logo" class="user-logo"> <!-- Logo Image -->
            <h1 class="header-title">Good Health and Well Being</h1> <!-- Header Title -->
        </div>
    
        <nav>
            <div class="menu-icon" onclick="toggleMenu()">
                <div class="bar"></div>
                <div class="bar"></div>
                <div class="bar"></div>
            </div>
            <ul>
                <li><a href="homepage.php">Home</a></li>
                <li class="dropdown">
                    <a href="services.html">Services</a>
                    <ul class="dropdown-content">
                        <li><a href="routine.html">Routine</a></li>
                        <li><a href="sleep.html">Sleep</a></li>
                        <li><a href="meal.html">Meal Plan</a></li>
                        <li><a href="stress.html">Stress & Anxiety Relief</a></li>
                    </ul>
                </li>
                <li><a href="contact.html">Contact</a></li>
                <li><a href="about.html">About Us</a></li>
            </ul>
        </nav>
    
      
    </header>
    

    <!-- Health Tracker Banner Section -->
    <section id="about" style="background-image: url('home.jpg'); background-size: cover; background-position: center; text-align: center; padding: 15%; color: white;">
    <div>
        <h1>Welcome to Your Health Tracker</h1>
        <?php 
            if(isset($_SESSION['email'])){
                $email = $_SESSION['email'];
                $query = mysqli_query($conn, "SELECT users.* FROM `users` WHERE users.email='$email'");
                while($row = mysqli_fetch_array($query)){
                    echo $row['firstName'].' '.$row['lastName'];
                }
            }
        ?>
        <p>Track your wellness and stay on top of your health</p>
        <a href="services.html" class="special-button">Explore Services</a>
        <a href="logout.php"class="special-button">Logout</a>
    </div>
</section>

    <section id="blog">
        <article>
            <h3>Healthy Meal Planning</h3>
            <img src="high-fiber-foods.png" alt="Healthy Meal Planning" class="blog-img">
            <p>For older adults, eating a balanced diet with nutrient-dense foods is essential. Focus on fruits, vegetables, whole grains, lean proteins, and fiber-rich foods to support digestion and heart health. Hydration is key, so drink water and eat water-rich foods. Smaller, frequent meals help maintain energy, while limiting processed foods, unhealthy fats, and sugars is important. Healthy fats from sources like avocados and fatty fish promote overall well-being.
            </p>
            <h2><a href="about.html" style="color: blue;">About Us</a></h2>
        </article>
        <article>
            <h3>Nourishing Your Golden Years: A Guide to Food Groups for Healthy Aging</h3>
            <img src="mealplan.jpeg" alt="Healthy Eating As You Age" class="blog-img">
            <p>A comprehensive guide to food groups for healthy aging.</p>
            <h3><a href="meal.html" style="color: blue;">Meal Plan</a></h3>
        </article>
        <article>
            <h4>Nourishing Your Golden Years: A Guide to Food Groups for Healthy Aging</h4>
            <img src="main.jpg" alt="Health Tracker Banner" class="blog-img">
            <h3><a href="for.html" style="color: blue;">For HTML</a></h3>
        </article>
    </section>
 
    
   
</div>
    <footer>
        <p>&copy; 2024 Good Health and Well Being. All rights reserved.</p>
    </footer>
    
    
    
   
      
</body>

</html>
