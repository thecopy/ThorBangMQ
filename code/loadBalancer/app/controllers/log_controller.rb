class LogController < ApplicationController
  skip_before_filter :verify_authenticity_token, :if => Proc.new { |c| c.request.format == 'application/json' }


  def show
    @logs = LogEntry.all
  end

  def create
    LogEntry.create(log_params)
    head 201
  end

  def delete_all
    LogEntry.delete_all

    head 204
  end

  def get_session

    @session_entries = LogEntry.where("log_session_id = #{params[:id]}").order('timestamp desc')

    result = ''
    @session_entries.each do |l|
      result += "#{l.timestamp} #{l.log_msg}"
    end

    send_data(result, :filename => "ThorBangMQ_session-#{params[:id]}.log")

  end

  private
    def log_params
      params.require(:log_entry).permit(:log_msg, :log_session_id, :timestamp)
    end
end
