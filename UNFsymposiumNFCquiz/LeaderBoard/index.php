<?php
// $name = $_GET['name'];
// $email = $_GET['email'];
// $score = $_GET['score'];

// // Create connection
// $conn = new mysqli($servername, $username, $password, $dbname);
// // Check connection
// if ($conn->connect_error) {
//     die("Connection failed: " . $conn->connect_error);
// }

// $sql = "INSERT INTO Score ([Score].name, [Score].email, [Score].score) VALUES ('$name', '$email', '$score')";

// if ($conn->query($sql) === true) {
//     echo "New record created successfully";
// } else {
//     echo "Error: " . $sql . "<br>" . $conn->error;
// }

// $conn->close();
?> 


<html>
<body>

Welcome <?php echo $_GET["name"]; ?><br>
Your email address is: <?php echo $_GET["email"]; ?>

</body>
</html>

