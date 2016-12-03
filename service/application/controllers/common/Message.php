<?php defined('BASEPATH') OR exit('No direct script access allowed');

/**
 * Message
 *
 * <pre>
 * 提供通知，消息查询接口
 * 
 * @package		CodeIgniter
 * @subpackage	Rest Server
 * @category	Controller
 * @author		xie.hj
 * @link		http://www.mifan.us
 * @response 响应码说明  资源编号+状态+编码
 * @response Notice资源编号为9，0000表示成功
 * @desc 响应编码ret		说明
 * 		 100000			成功
 * 		 904001			较验不通过，用户名或密码为空 、 userid和最后一次登录时间不能为空、邮箱不合法等
 * 		 904002			
 * 		 904003			
 * 		 904004			
 *		 904005			  
 * 		 904006			
 * 		 904007			
 * 		 904009			其他错误
 * </pre>
 */

class Message extends MF_Controller
{
		
	public function __construct(){
		parent::__construct();
		$this->load->helper('email');
		$this->load->model('user_model');
		$this->load->model('message_model');
	}
	
	/**
	 * 查看指定消息
	 * 
	 * <pre>
	 * 请求参数
	 * 					必选		参数范围			说明
	 * noticeid 		true 	int				消息ID
	 * </pre>
	 * 
	 * @return	json
	 * 
	 */
	public function message_get(){
		$this->hooks->call_hook('acl_auth');
    	
		$data['userid'] = $this->get_post('userid');
		$data['messageid'] = $this->get('messageid');
		
        if(!$data['messageid']){
        	$message = array( 'ret' => 904001, 'msg' => '请选择你要查看的消息!');
            $this->response($message, 200); 
        }
		
        list($ret,$result) = $this->message_model->get_source_by_id('_get_message_by_id',$data['messageid']);
		if($ret != '100000'){
			 $message = array( 'ret' => $ret, 'msg' => $result);
             $this->response($message, 200); 
		}
		
		$message = array('ret' => $ret,'msg' => 'ok','result' => $result);
		$this->response($message, 200); 
	
    }
	
	public function msgcount_get(){
		$data['userid'] = $this->get('userid');
		
		list($ret,$result) = $this->message_model->msg_count($data['userid'],$data);
		if($ret != '100000'){
			 $message = array( 'ret' => $ret, 'msg' => $result,'result' => 0);
             $this->response($message, 200); 
		}
		
		$message = array('ret' => $ret,'msg' => 'ok','result' => $result);
		$this->response($message, 200); 
	}
	
}