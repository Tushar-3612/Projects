<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require __DIR__ . '/phpmailer/src/Exception.php';
require __DIR__ . '/phpmailer/src/PHPMailer.php';
require __DIR__ . '/phpmailer/src/SMTP.php';

if($_SERVER["REQUEST_METHOD"] == "POST") {
    $name = trim($_POST['name']);       
    $email = trim($_POST['email']);
    $phone = trim($_POST['phone']);
    $service = trim($_POST['service']);
    $message = trim($_POST['message']);

    // ===== Save to CSV =====
    $file = 'Services.csv';
    if(!file_exists($file)) {
        $header = ['Timestamp','Full Name','Email','Phone','Service','Message'];
        $fp = fopen($file, 'a');
        fputcsv($fp, $header);
        fclose($fp);
    }
    $data = [date("Y-m-d H:i:s"), $name, $email, $phone, $service, $message];
    $fp = fopen($file, 'a');
    fputcsv($fp, $data);
    fclose($fp);

    // ===== Show success message immediately =====
    echo "
    <div style='text-align:center; margin-top:50px;'>
        <h2 style='color:green; font-family:Times New Roman, sans-serif;'>
             ✅ Thank you for reaching out!<br>
             Your request has been received successfully.<br>
             Our team will get in touch with you shortly.
        </h2>
        <a href='../services.html' style='
            display:inline-block;
            margin-top:20px;
            padding:10px 20px;
            background-color:#ffbf00;
            color:black;
            text-decoration:none;
            border-radius:5px;
            font-weight:bold;
            font-family:Arial, sans-serif;
        '>Go Back</a>
    </div>
    ";

    // ===== Flush output to user =====
    if(function_exists('fastcgi_finish_request')) {
        fastcgi_finish_request();
    }

    // ===== Send Emails in Background =====
    $smtpUser = 'tusharnagare7875@gmail.com';
    $smtpPass = 'hnxpzznccbwaioun';

    // ----- Admin Email -----
    try {
        $adminMail = new PHPMailer(true);
        $adminMail->isSMTP();
        $adminMail->Host = 'smtp.gmail.com';
        $adminMail->SMTPAuth = true;
        $adminMail->Username = $smtpUser;
        $adminMail->Password = $smtpPass;
        $adminMail->SMTPSecure = 'tls';
        $adminMail->Port = 587;

        $adminMail->setFrom($smtpUser, 'Website Form');
        $adminMail->addAddress($smtpUser, 'Admin'); // your Gmail
        $adminMail->addReplyTo($email, $name);

        $adminMail->isHTML(true);
        $adminMail->Subject = "📩 New Service Request from $name";
        $adminMail->Body = "
            <h2>New Service Request</h2>
            <p><b>Name:</b> $name</p>
            <p><b>Email:</b> $email</p>
            <p><b>Phone:</b> $phone</p>
            <p><b>Service:</b> $service</p>
            <p><b>Message:</b> $message</p>
            <p><small>Received on ".date('Y-m-d H:i:s')."</small></p>
        ";
        $adminMail->send();
    } catch (Exception $e) {
        error_log("Admin Email Error: {$adminMail->ErrorInfo}");
    }

    // ----- User Confirmation Email -----
    try {
        $userMail = new PHPMailer(true);
        $userMail->isSMTP();
        $userMail->Host = 'smtp.gmail.com';
        $userMail->SMTPAuth = true;
        $userMail->Username = $smtpUser;
        $userMail->Password = $smtpPass;
        $userMail->SMTPSecure = 'tls';
        $userMail->Port = 587;

        $userMail->setFrom($smtpUser, 'Your Company');
        $userMail->addAddress($email, $name);
        $userMail->addReplyTo($smtpUser, 'Support');

        $userMail->isHTML(true);
        $userMail->Subject = "Thank You for Contacting Us!";
        $userMail->Body = "
            <h3>Hello $name,</h3>
            <p>Thank you for contacting us regarding <b>$service</b>.</p>
            <p>We have received your message and will get back to you shortly.</p>
            <p><b>Your Message:</b> $message</p>
            <p>Best Regards,<br>Your Company</p>
        ";
        $userMail->send();
    } catch (Exception $e) {
        error_log("User Email Error: {$userMail->ErrorInfo}");
    }
}
?>
