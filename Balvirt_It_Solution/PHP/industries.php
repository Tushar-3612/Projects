<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require __DIR__ . '/phpmailer/src/Exception.php';
require __DIR__ . '/phpmailer/src/PHPMailer.php';
require __DIR__ . '/phpmailer/src/SMTP.php';

if($_SERVER["REQUEST_METHOD"] == "POST") {

    // ===== Collect form data =====
    $name = trim($_POST['name']);       
    $userEmail = trim($_POST['email']);
    $phone = trim($_POST['phone']);
    $industry = trim($_POST['industry']);
    $message = trim($_POST['message']);

    // ===== Save in CSV =====
    $file = __DIR__ . '/industries.csv';
    if(!file_exists($file)) {
        $fp = fopen($file, 'a');
        fputcsv($fp, ['Timestamp','Full Name','Email','Phone','Industry','Message']);
        fclose($fp);
    }
    $fp = fopen($file, 'a');
    fputcsv($fp, [date("Y-m-d H:i:s"), $name, $userEmail, $phone, $industry, $message]);
    fclose($fp);

    // ===== SMTP settings =====
    $smtpUser = 'tusharnagare7875@gmail.com'; // 👈 Gmail
    $smtpPass = 'hnxpzznccbwaioun';          // 👈 16-char App Password (no spaces)

    // ===== Send email to Admin =====
    $adminMail = new PHPMailer(true);
    try {
        $adminMail->isSMTP();
        $adminMail->Host = 'smtp.gmail.com';
        $adminMail->SMTPAuth = true;
        $adminMail->Username = $smtpUser;
        $adminMail->Password = $smtpPass;
        $adminMail->SMTPSecure = 'tls';
        $adminMail->Port = 587;

        $adminMail->setFrom($smtpUser, 'Website Form');
        $adminMail->addAddress($smtpUser, 'Admin'); // Admin email
        $adminMail->addReplyTo($userEmail, $name);

        $adminMail->isHTML(true);
        $adminMail->Subject = "New Inquiry from $name";
        $adminMail->Body = "
            <h2>New Contact Request</h2>
            <p><b>Name:</b> $name</p>
            <p><b>Email:</b> $userEmail</p>
            <p><b>Phone:</b> $phone</p>
            <p><b>Industry:</b> $industry</p>
            <p><b>Message:</b> $message</p>
            <p><small>Received on ".date('Y-m-d H:i:s')."</small></p>
        ";
        $adminMail->send();
    } catch (Exception $e) {
        echo "<p style='color:red;'>Admin Email Error: {$adminMail->ErrorInfo}</p>";
    }

    // ===== Send confirmation email to User =====
    $userMail = new PHPMailer(true);
    try {
        $userMail->isSMTP();
        $userMail->Host = 'smtp.gmail.com';
        $userMail->SMTPAuth = true;
        $userMail->Username = $smtpUser;
        $userMail->Password = $smtpPass;
        $userMail->SMTPSecure = 'tls';
        $userMail->Port = 587;

        $userMail->setFrom($smtpUser, 'Balvirt');
        $userMail->addAddress($userEmail, $name);
        $userMail->addReplyTo($smtpUser, 'Support');

        $userMail->isHTML(true);
        $userMail->Subject = "Thank You for Contacting Us!";
        $userMail->Body = "
            <h3>Hello $name,</h3>
            <p>Thank you for contacting us regarding <b>$industry</b>.</p>
            <p>We have received your message and will get back to you shortly.</p>
            <p><b>Your Message:</b> $message</p>
            <p>Best Regards,<br>Your Company</p>
        ";
        $userMail->send();
    } catch (Exception $e) {
        echo "<p style='color:red;'>User Email Error: {$userMail->ErrorInfo}</p>";
    }

    // ===== Show Success Message =====
    echo "
    <div style='text-align:center; margin-top:50px; font-family:Arial, sans-serif;'>
        <h2 style='color:green;'>✅ Thank you, $name!</h2>
        <p>Your message has been submitted successfully.<br>Check your email for confirmation.</p>
        <a href='../industries.html' style='
            display:inline-block;
            margin-top:20px;
            padding:10px 20px;
            background-color:#ffbf00;
            color:black;
            text-decoration:none;
            border-radius:5px;
            font-weight:bold;
        '>Go Back</a>
    </div>
    ";
}
?>
