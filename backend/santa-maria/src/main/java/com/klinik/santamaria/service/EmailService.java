package com.klinik.santamaria.service;

import com.klinik.santamaria.helper.UsersDto;
import com.klinik.santamaria.model.Users;
import com.klinik.santamaria.repository.UsersDao;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired
  private UsersDao usersDao;

  @Autowired
  private JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String senderEmail;

  public void sendEmail(UsersDto to)
      throws MessagingException, UnsupportedEncodingException {

    String toAddress = to.getEmail();
    String fromAddress = senderEmail;
    String senderName = "Klinik Santa Maria";

    String subject = "Informasi pendaftaran user";

    String content = "Kepada <b> [[name]] </b>,<br>"
        + "Akun anda berhasil di daftarkan ,silahkan login dengan akun sebagai berikut : <br> "
        + "&nbsp; &nbsp; &nbsp; <b> username : [[username]] </b> <br>"
        + " &nbsp; &nbsp; &nbsp; <b> password : [[password]] </b> <br>"
        + "<br>"
        + "Terima kasih,<br>"
        + "<h4><b>Klinik Santa Maria </b></h4>";

    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

    helper.setFrom(fromAddress, senderName);
    helper.setTo(toAddress);
    helper.setSubject(subject);

    content = content.replace("[[name]]",to.getFullName());
    content = content.replace("[[username]]",to.getUsername());
    content = content.replace("[[password]]",to.getPassword());

    helper.setText(content, true);

    javaMailSender.send(mimeMessage);
  }
}
