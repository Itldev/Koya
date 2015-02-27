<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      You have been invited to join the '${invitation.siteName}' koya company 
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hi ${invitee.firstName!""},</p>
      
                                             <p>${inviter.firstName!""} ${inviter.lastName!""} 
                                             has invited you to join the <b>${invitation.siteName}</b> site with the role of ${invitation.inviteeSiteRole}.</p>
                                             
                                             <p>Click this link to accept ${inviter.firstName!""}'s invitation:<br />
                                             <br /><a href="${invitation.acceptLink}">${invitation.acceptLink}</a></p>
                                             
                                             <#if invitation.inviteeGenPassword?exists>
                                             <p>An account has been created for you and your login details are:<br />
                                             <br />Username: <b>${invitation.inviteeUserName}</b>
                                             <br />Password: <b>${invitation.inviteeGenPassword}</b>
                                             </p>
                                             
                                             <p><b>We strongly advise you to change your password when you log in for the first time.</b><br />
                                             You can do this by going to <b>My Profile</b> and selecting <b>Change Password</b>.</p>
                                             </#if>
                                             
                                             <p>If you want to decline ${inviter.firstName!""}’s invitation, click this link:<br />
                                             <br /><a href="${invitation.rejectLink}">${invitation.rejectLink}</a></p>
                                             
                                             <p>Sincerely,<br />
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>