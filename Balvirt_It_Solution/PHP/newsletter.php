<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require __DIR__ . '/phpmailer/src/Exception.php';
require __DIR__ . '/phpmailer/src/PHPMailer.php';
require __DIR__ . '/phpmailer/src/SMTP.php';

if($_SERVER["REQUEST_METHOD"] == "POST" && isset($_POST['email'])) {
    $email = trim($_POST['email']);

    // ===== Save email to CSV =====
    $file = 'newsletter.csv';
    if(!file_exists($file)) {
        $fp = fopen($file, 'a');
        fputcsv($fp, ['Timestamp','Email']);
        fclose($fp);
    }
    $fp = fopen($file, 'a');
    fputcsv($fp, [date("Y-m-d H:i:s"), $email]);
    fclose($fp);

    // ===== Show instant success message =====
    echo "Get ready for exciting updates, tips, and exclusive content delivered straight to your inbox.";

    // ===== Flush output =====
    if(function_exists('fastcgi_finish_request')) {
        fastcgi_finish_request();
    }

    // ===== Send confirmation email in background =====
    $smtpUser = 'tusharnagare7875@gmail.com';
    $smtpPass = 'hnxpzznccbwaioun';

    try {
        $mail = new PHPMailer(true);
        $mail->isSMTP();
        $mail->Host = 'smtp.gmail.com';
        $mail->SMTPAuth = true;
        $mail->Username = $smtpUser;
        $mail->Password = $smtpPass;
        $mail->SMTPSecure = 'tls';
        $mail->Port = 587;

        $mail->setFrom($smtpUser, 'Your Company');
        $mail->addAddress($email);
        $mail->addReplyTo($smtpUser, 'Support');

        $mail->isHTML(true);
        $mail->Subject = "Newsletter Subscription Confirmed";
        $mail->Body = "
            <h3>Hello!</h3>
            <p>Thank you for subscribing to our newsletter.</p>
            <p>Stay tuned for updates and news.</p>
            <p>Best Regards,<br>Your Company</p>
        ";
        $mail->send();
    } catch (Exception $e) {
        error_log("Newsletter Email Error: {$mail->ErrorInfo}");
    }
} else {
    // Optional: handle direct access
    echo "❌ Invalid request.";
}
?>
